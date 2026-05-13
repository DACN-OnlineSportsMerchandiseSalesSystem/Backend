package com.javaweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.ChatProductCardDTO;
import com.javaweb.entity.Category;
import com.javaweb.entity.Product;
import com.javaweb.entity.ProductImage;
import com.javaweb.entity.ProductVariant;
import com.javaweb.repository.ProductRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static dev.langchain4j.data.message.UserMessage.userMessage;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private static final String SYSTEM_SPORTBOT =
            "Bạn là SportBot, trợ lý tư vấn khách hàng chuyên nghiệp của SportZone.\n" +
            "QUY ĐỊNH:\n" +
            "- Trả lời TRỰC TIẾP câu hỏi, tự nhiên, thân thiện, ngắn gọn.\n" +
            "- TUYỆT ĐỐI GIỮ NGUYÊN tên thương hiệu, tên sản phẩm tiếng Anh (Nike, Adidas, Yonex...). KHÔNG phiên âm.\n" +
            "- KHÔNG bắt đầu bằng 'Dựa trên thông tin...' hay 'Theo ngữ cảnh...'.\n" +
            "- Có thể dùng emoji phù hợp.\n\n" +
            "--- TÍNH NĂNG ĐẶC BIỆT: THÊM VÀO GIỎ HÀNG ---\n" +
            "Nếu khách hàng bày tỏ ý định mua hàng RÕ RÀNG (ví dụ: 'lấy cho tôi', 'thêm vào giỏ', 'tôi muốn mua'),\n" +
            "TRƯỚC TIÊN hãy trả lời bằng text như bình thường.\n" +
            "SAU ĐÓ, ở DÒNG CUỐI CÙNG của câu trả lời, thêm một JSON action theo đúng định dạng sau:\n" +
            "%%ACTION:{\"type\":\"ADD_TO_CART\",\"variantId\":ID_BIẾN_THỂ,\"quantity\":SỐ_LƯỢNG}%%\n" +
            "Nếu không chắc variantId, hãy hỏi khách chọn size/màu trước.\n" +
            "Nếu khách CHƯA ĐĂNG NHẬP, hãy nhắc: 'Bạn cần đăng nhập để thêm vào giỏ hàng nhé!'.\n" +
            "Nếu KHÔNG có ý định mua hàng, TUYỆT ĐỐI KHÔNG sinh ra JSON action.";

    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ProductRepository productRepository;
    private final ChatHistoryService chatHistoryService;
    private final CartService cartService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotService(ChatLanguageModel chatLanguageModel,
                          StreamingChatLanguageModel streamingChatLanguageModel,
                          EmbeddingModel embeddingModel,
                          EmbeddingStore<TextSegment> embeddingStore,
                          ProductRepository productRepository,
                          ChatHistoryService chatHistoryService,
                          CartService cartService) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.productRepository = productRepository;
        this.chatHistoryService = chatHistoryService;
        this.cartService = cartService;
    }

    // =====================================================
    // Hàm truy vấn Vector DB + trả về cả danh sách productId tìm được
    // =====================================================
    private record VectorResult(String context, List<Long> productIds) {}

    private VectorResult retrieveFromVectorDb(String question, int maxResults) {
        try {
            Embedding queryEmbedding = embeddingModel.embed(question).content();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, maxResults, 0.45);

            if (matches == null || matches.isEmpty()) return new VectorResult("", List.of());

            List<Long> productIds = new ArrayList<>();
            StringBuilder context = new StringBuilder();

            for (EmbeddingMatch<TextSegment> match : matches) {
                context.append(match.embedded().text()).append("\n---\n");
                String type = match.embedded().metadata().getString("type");
                String idStr = match.embedded().metadata().getString("id");
                if ("product".equals(type) && idStr != null) {
                    try { productIds.add(Long.parseLong(idStr)); } catch (NumberFormatException ignored) {}
                }
            }
            return new VectorResult(context.toString(), productIds);
        } catch (Exception e) {
            log.warn("Không thể truy vấn Vector DB: {}", e.getMessage());
            return new VectorResult("", List.of());
        }
    }

    // =====================================================
    // Lấy thông tin sản phẩm chi tiết từ SQL DB
    // =====================================================
    private String buildProductContext(Long productId) {
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty()) return "";

        Product p = optProduct.get();
        StringBuilder sb = new StringBuilder();
        sb.append("Sản phẩm khách đang xem:\n");
        sb.append("- Tên: ").append(p.getName()).append("\n");
        sb.append("- Thương hiệu: ").append(p.getBrand() != null ? p.getBrand().getName() : "N/A").append("\n");
        sb.append("- Danh mục: ").append(
                p.getCategories() != null && !p.getCategories().isEmpty()
                        ? p.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
                        : "N/A"
        ).append("\n");
        sb.append("- Mã SP: ").append(p.getProductCode()).append("\n");
        sb.append("- Mô tả: ").append(p.getDescription() != null ? p.getDescription() : "Không có").append("\n");

        if (p.getProductVariants() != null && !p.getProductVariants().isEmpty()) {
            sb.append("- Phiên bản có sẵn (dùng variantId để thêm vào giỏ):\n");
            p.getProductVariants().forEach(v ->
                sb.append("  • variantId=").append(v.getId())
                        .append(" | Size ").append(v.getSize())
                        .append(", Màu ").append(v.getColor())
                        .append(", Giá: ").append(v.getPrice()).append("đ")
                        .append(", Tồn: ").append(v.getStockQuantity()).append("\n")
            );
        }
        return sb.toString();
    }

    // =====================================================
    // Tạo danh sách Product Cards từ danh sách ID
    // =====================================================
    private List<ChatProductCardDTO> buildProductCards(List<Long> productIds) {
        if (productIds.isEmpty()) return List.of();

        List<Product> products = productRepository.findAllById(productIds);
        return products.stream().map(p -> {
            String imageUrl = p.getProductImages() != null
                    ? p.getProductImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                            .findFirst()
                            .map(ProductImage::getImageUrl)
                            .orElseGet(() -> p.getProductImages().stream()
                                    .findFirst()
                                    .map(ProductImage::getImageUrl)
                                    .orElse(null))
                    : null;

            BigDecimal price = p.getProductVariants() != null && !p.getProductVariants().isEmpty()
                    ? p.getProductVariants().stream()
                            .findFirst()
                            .map(ProductVariant::getPrice)
                            .orElse(BigDecimal.ZERO)
                    : BigDecimal.ZERO;

            return new ChatProductCardDTO(
                    p.getId(),
                    p.getName(),
                    p.getSlug(),
                    p.getBrand() != null ? p.getBrand().getName() : "",
                    price,
                    imageUrl
            );
        }).collect(Collectors.toList());
    }

    // =====================================================
    // TRƯỜNG HỢP A: Hybrid RAG — Trang Chi tiết Sản phẩm
    // =====================================================
    public void streamHybrid(String message, Long productId, String sessionId, String userEmail, SseEmitter emitter) {
        String historyContext = (sessionId != null) ? chatHistoryService.getHistoryAsContext(sessionId) : "";
        String productContext = buildProductContext(productId);
        VectorResult vectorResult = retrieveFromVectorDb(message, 3);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_SPORTBOT).append("\n\n");
        if (!historyContext.isEmpty()) promptBuilder.append(historyContext);
        if (!productContext.isEmpty()) promptBuilder.append("[THÔNG TIN SẢN PHẨM]\n").append(productContext).append("\n");
        if (!vectorResult.context().isEmpty()) promptBuilder.append("[KIẾN THỨC BỔ SUNG]\n").append(vectorResult.context()).append("\n");
        if (userEmail == null) promptBuilder.append("[LƯU Ý]: Khách hàng CHƯA ĐĂNG NHẬP. Không thể thêm vào giỏ hàng.\n\n");
        promptBuilder.append("[CÂU HỎI CỦA KHÁCH HÀNG]\n").append(message);

        log.info(">>> [HYBRID] productId={} | session={} | user={}", productId, sessionId, userEmail);
        doStream(promptBuilder.toString(), message, sessionId, userEmail, vectorResult.productIds(), emitter);
    }

    // =====================================================
    // TRƯỜNG HỢP B: Pure RAG — Ngoài trang Sản phẩm
    // =====================================================
    public void streamPureRag(String message, String sessionId, String userEmail, SseEmitter emitter) {
        String historyContext = (sessionId != null) ? chatHistoryService.getHistoryAsContext(sessionId) : "";
        VectorResult vectorResult = retrieveFromVectorDb(message, 4);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_SPORTBOT).append("\n\n");
        if (!historyContext.isEmpty()) promptBuilder.append(historyContext);
        if (!vectorResult.context().isEmpty()) promptBuilder.append("[KIẾN THỨC TỪ KHO DỮ LIỆU SPORTZONE]\n").append(vectorResult.context()).append("\n\n");
        if (userEmail == null) promptBuilder.append("[LƯU Ý]: Khách hàng CHƯA ĐĂNG NHẬP. Không thể thêm vào giỏ hàng.\n\n");
        promptBuilder.append("[CÂU HỎI CỦA KHÁCH HÀNG]\n").append(message);

        log.info(">>> [PURE RAG] session={} | user={}", sessionId, userEmail);
        doStream(promptBuilder.toString(), message, sessionId, userEmail, vectorResult.productIds(), emitter);
    }

    // =====================================================
    // Hàm Streaming chính — tích hợp Memory + Function Calling + Generative UI
    // =====================================================
    private void doStream(String fullPrompt, String originalMessage, String sessionId,
                          String userEmail, List<Long> suggestedProductIds, SseEmitter emitter) {

        List<ChatMessage> messages = List.of(userMessage(fullPrompt));
        StringBuilder botResponseBuilder = new StringBuilder();

        // Lưu tin nhắn của user vào Redis
        if (sessionId != null) {
            chatHistoryService.saveTurn(sessionId, "user", originalMessage);
        }

        streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    botResponseBuilder.append(token);
                    // Chỉ stream token text ra ngoài (loại bỏ phần action JSON nếu có)
                    if (!token.contains("%%ACTION:")) {
                        emitter.send(SseEmitter.event().name("token").data(token));
                    }
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    String fullBotResponse = botResponseBuilder.toString();

                    // === Feature 1: Lưu lịch sử vào Redis ===
                    String cleanResponse = fullBotResponse.replaceAll("%%ACTION:\\{.*?\\}%%", "").trim();
                    if (sessionId != null) {
                        chatHistoryService.saveTurn(sessionId, "bot", cleanResponse);
                    }

                    // === Feature 2: Phát hiện và thực thi Action (Function Calling) ===
                    java.util.regex.Matcher actionMatcher = java.util.regex.Pattern
                            .compile("%%ACTION:(\\{.*?\\})%%")
                            .matcher(fullBotResponse);

                    if (actionMatcher.find() && userEmail != null) {
                        String actionJson = actionMatcher.group(1);
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> action = objectMapper.readValue(actionJson, Map.class);
                            String actionType = (String) action.get("type");

                            if ("ADD_TO_CART".equals(actionType)) {
                                Long variantId = Long.parseLong(action.get("variantId").toString());
                                int quantity = Integer.parseInt(action.get("quantity").toString());

                                AddCartRequestDTO cartRequest = new AddCartRequestDTO();
                                cartRequest.setProductVariantId(variantId);
                                cartRequest.setQuantity(quantity);

                                cartService.addCartItem(cartRequest, userEmail);
                                log.info(">>> [FUNCTION CALL] Đã thêm variantId={} x{} vào giỏ của {}", variantId, quantity, userEmail);

                                // Báo hiệu Frontend cập nhật giỏ hàng
                                emitter.send(SseEmitter.event().name("cart_updated").data("true"));
                            }
                        } catch (Exception ex) {
                            log.error("Lỗi thực thi Action: {}", ex.getMessage());
                        }
                    }

                    // === Feature 3: Generative UI — Gửi Product Cards ===
                    if (!suggestedProductIds.isEmpty()) {
                        List<ChatProductCardDTO> cards = buildProductCards(suggestedProductIds);
                        if (!cards.isEmpty()) {
                            String cardsJson = objectMapper.writeValueAsString(cards);
                            emitter.send(SseEmitter.event().name("product_cards").data(cardsJson));
                            log.info(">>> [GENERATIVE UI] Gửi {} product cards.", cards.size());
                        }
                    }

                    emitter.complete();
                } catch (Exception e) {
                    log.error("Lỗi khi hoàn thành stream: {}", e.getMessage());
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Stream error: {}", error.getMessage());
                emitter.completeWithError(error);
            }
        });
    }

    // =====================================================
    // Phương thức đồng bộ — dùng cho lời chào giới thiệu sản phẩm
    // =====================================================
    public com.javaweb.dto.ChatbotResponse chat(String message) {
        VectorResult vectorResult = retrieveFromVectorDb(message, 3);
        String prompt = SYSTEM_SPORTBOT + "\n\n" +
                (vectorResult.context().isEmpty() ? "" : "[KIẾN THỨC]\n" + vectorResult.context() + "\n\n") +
                "[CÂU HỎI]\n" + message;

        String raw = chatLanguageModel.generate(prompt);
        return new com.javaweb.dto.ChatbotResponse(raw, raw);
    }
}
