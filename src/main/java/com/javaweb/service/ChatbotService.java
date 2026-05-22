package com.javaweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.ChatProductCardDTO;
import com.javaweb.dto.ProductDTO;
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
            "- TUYỆT ĐỐI CHỈ TƯ VẤN CÁC SẢN PHẨM CÓ THẬT TRONG [KIẾN THỨC BỔ SUNG], [THÔNG TIN SẢN PHẨM] HOẶC [THÔNG TIN TỪ HỆ THỐNG]. Nếu không tìm thấy thông tin trong các mục trên, BẮT BUỘC TRẢ LỜI: 'Hiện tại hệ thống không có thông tin về mẫu này.'. KHÔNG ĐƯỢC TỰ SUY DIỄN THÊM SẢN PHẨM HAY GIÁ BÊN NGOÀI.\n" +
            "- TUYỆT ĐỐI KHÔNG BỊA ĐẶT THÔNG TIN CHÍNH SÁCH HAY ĐỊA CHỈ. Nếu khách hỏi các vấn đề ngoài lề (như quán ăn, thời tiết...) hoặc chính sách, địa chỉ mà không có trong [KIẾN THỨC BỔ SUNG], BẮT BUỘC TRẢ LỜI: 'Hiện tại hệ thống không có thông tin về vấn đề này.' sau đó LỊCH SỰ DẪN DẮT KHÁCH HÀNG QUAY LẠI CHỦ ĐỀ CÁC SẢN PHẨM THỂ THAO.\n" +
            "- KHI BÁO GIÁ: HÃY LUÔN ƯU TIÊN SỬ DỤNG GIÁ TRONG [THÔNG TIN SẢN PHẨM] vì đó là giá khuyến mãi mới nhất. Nếu [THÔNG TIN SẢN PHẨM] có giá thấp hơn [KIẾN THỨC BỔ SUNG], hãy nói cho khách biết họ đang được giảm giá.\n" +
            "- VIẾT ĐÚNG VÀ ĐẦY ĐỦ TÊN SẢN PHẨM. Không được tự ý rút gọn tên (ví dụ: Pegasus 3S không được viết thành us 3S).\n" +
            "- TRÌNH BÀY RÕ RÀNG: Bắt buộc trình bày mỗi sản phẩm trên một dòng riêng biệt theo định dạng: '- **[Tên sản phẩm đầy đủ]** - Giá: [Giá tiền]'. Nếu có mô tả thì viết tiếp vào cùng dòng. Tuyệt đối không để thừa dấu gạch ngang '-' ở cuối câu hoặc viết dính liền chữ.\n" +
            "- TUYỆT ĐỐI GIỮ NGUYÊN tên thương hiệu, tên sản phẩm tiếng Anh (Nike, Adidas, Yonex...). KHÔNG phiên âm.\n" +
            "- KHÔNG bắt đầu bằng 'Dựa trên thông tin...' hay 'Theo ngữ cảnh...'.\n" +
            "- Có thể dùng emoji phù hợp.\n\n" +
            "--- TÍNH NĂNG ĐẶC BIỆT: THÊM VÀO GIỎ HÀNG ---\n" +
            "QUY TRÌNH 2 BƯỚC BẮT BUỘC:\n" +
            "1. BƯỚC XÁC NHẬN: Khi khách hàng yêu cầu mua hàng (ví dụ: 'tôi muốn mua', 'thêm vào giỏ', 'lấy cho mình', 'ok thêm đi'), BẠN BẮT BUỘC PHẢI HỎI LẠI XÁC NHẬN: 'Bạn có muốn mình thêm [Tên SP] size [Size], màu [Màu] vào giỏ hàng không?' và DỪNG LẠI (Tuyệt đối không sinh mã JSON lúc này). NẾU KHÁCH CHỈ HỎI THÔNG TIN TƯ VẤN, chỉ được hỏi gợi mở: 'Bạn có muốn tham khảo chi tiết hay chọn màu/size không?'.\n" +
            "2. BƯỚC THỰC THI: CHỈ KHI NÀO khách hàng ĐỒNG Ý SAU KHI BẠN ĐÃ HỎI CÂU XÁC NHẬN Ở BƯỚC 1, bạn BẮT BUỘC PHẢI thông báo 'Đã thêm sản phẩm vào giỏ hàng thành công!' rồi MỚI ĐƯỢC sinh ra JSON action ở cuối cùng.\n\n" +
            "ĐỊNH DẠNG ACTION (Chỉ dùng ở Bước 2, nằm ở cuối cùng câu trả lời):\n" +
            "%%ACTION:{\"type\":\"ADD_TO_CART\",\"variantId\":ID_BIẾN_THỂ,\"quantity\":SỐ_LƯỢNG}%%\n" +
            "LƯU Ý: Tuyệt đối không viết gì sau dấu %% kết thúc action.\n" +
            "Nếu khách CHƯA ĐĂNG NHẬP, hãy nhắc: 'Bạn cần đăng nhập để thêm vào giỏ hàng nhé!'.\n" +
            "Nếu KHÔNG có ý định mua hàng, TUYỆT ĐỐI KHÔNG sinh ra JSON action.";

    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ProductRepository productRepository;
    private final ChatHistoryService chatHistoryService;
    private final CartService cartService;
    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotService(ChatLanguageModel chatLanguageModel,
                          StreamingChatLanguageModel streamingChatLanguageModel,
                          EmbeddingModel embeddingModel,
                          EmbeddingStore<TextSegment> embeddingStore,
                          ProductRepository productRepository,
                          ChatHistoryService chatHistoryService,
                          CartService cartService,
                          ProductService productService) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.productRepository = productRepository;
        this.chatHistoryService = chatHistoryService;
        this.cartService = cartService;
        this.productService = productService;
    }

    private record VectorResult(String context, List<Long> productIds) {}

    private VectorResult retrieveFromVectorDb(String question, int maxResults) {
        try {
            Embedding queryEmbedding = embeddingModel.embed(question).content();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, maxResults, 0.2);

            if (matches == null || matches.isEmpty()) return new VectorResult("", List.of());

            List<Long> productIds = new ArrayList<>();
            StringBuilder context = new StringBuilder();

            for (EmbeddingMatch<TextSegment> match : matches) {
                context.append(match.embedded().text()).append("\n---\n");
                String type = match.embedded().metadata().getString("type");
                String idStr = match.embedded().metadata().getString("id");
                if ("product".equals(type) && idStr != null) {
                    try {
                        productIds.add(Long.parseLong(idStr));
                    } catch (NumberFormatException ignored) {}
                }
            }
            return new VectorResult(context.toString(), productIds.stream().distinct().collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Lỗi khi retrieve từ Vector DB: {}", e.getMessage());
            return new VectorResult("", List.of());
        }
    }

    private String buildProductContext(Long productId) {
        if (productId == null) return "";
        try {
            ProductDTO dto = productService.getProductById(productId);
            if (dto != null) {
                return formatProductDetailsFromDTO(dto);
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy ProductDTO cho chatbot: {}", e.getMessage());
        }
        return "";
    }

    private String formatProductDetailsFromDTO(ProductDTO p) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Tên sản phẩm: ").append(p.getName()).append("\n");
        sb.append("- Thương hiệu: ").append(p.getBrandName() != null ? p.getBrandName() : "N/A").append("\n");
        sb.append("- Danh mục: ").append(
                p.getCategoryNames() != null && !p.getCategoryNames().isEmpty()
                        ? String.join(", ", p.getCategoryNames())
                        : "N/A"
        ).append("\n");
        sb.append("- Mã SP: ").append(p.getProductCode()).append("\n");
        sb.append("- Mô tả: ").append(p.getDescription() != null ? p.getDescription() : "Không có").append("\n");

        if (p.getVariants() != null && !p.getVariants().isEmpty()) {
            sb.append("- Phiên bản có sẵn (dùng variantId để thêm vào giỏ):\n");
            p.getVariants().forEach(v -> {
                String priceStr = v.getPrice() != null ? String.valueOf(v.getPrice().longValue()) : "Liên hệ";
                sb.append("  + variantId=").append(v.getId())
                        .append(" | Size ").append(v.getSize())
                        .append(", Màu ").append(v.getColor())
                        .append(", Giá: ").append(priceStr)
                        .append(", Tồn: ").append(v.getStockQuantity()).append("\n");
            });
        }
        return sb.toString();
    }

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

    private List<ProductDTO> getTopSellingIfRequested(String message) {
        String lowerMsg = message.toLowerCase();
        if (lowerMsg.contains("bán chạy") || lowerMsg.contains("hot") || lowerMsg.contains("best seller") || lowerMsg.contains("bán nhiều nhất")) {
            try {
                return productService.getTopSellingProductsPublic(4);
            } catch (Exception e) {
                log.warn("Lỗi khi lấy top selling cho chatbot: {}", e.getMessage());
            }
        }
        return List.of();
    }

    public void streamHybrid(String message, Long productId, String sessionId, String userEmail, SseEmitter emitter) {
        String historyContext = (sessionId != null) ? chatHistoryService.getHistoryAsContext(sessionId) : "";
        String productContext = buildProductContext(productId);
        VectorResult vectorResult = retrieveFromVectorDb(message, 30);
        List<ProductDTO> topSelling = getTopSellingIfRequested(message);
        List<Long> finalProductIds = new ArrayList<>(vectorResult.productIds());

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_SPORTBOT).append("\n\n");
        if (!historyContext.isEmpty()) promptBuilder.append(historyContext);
        if (!productContext.isEmpty()) promptBuilder.append("[THÔNG TIN SẢN PHẨM]\n").append(productContext).append("\n");
        
        if (!topSelling.isEmpty()) {
            promptBuilder.append("[THÔNG TIN TỪ HỆ THỐNG: SẢN PHẨM BÁN CHẠY NHẤT]\n");
            for(ProductDTO p : topSelling) {
                String priceStr = p.getPrice() != null ? String.valueOf(p.getPrice().longValue()) : "Liên hệ";
                promptBuilder.append("- ").append(p.getName()).append(" (Mã: ").append(p.getProductCode()).append(") - Giá: ").append(priceStr).append("\n");
                if(!finalProductIds.contains(p.getId())) finalProductIds.add(p.getId());
            }
            promptBuilder.append("\n");
        }
        
        if (!vectorResult.context().isEmpty()) promptBuilder.append("[KIẾN THỨC BỔ SUNG]\n").append(vectorResult.context()).append("\n");
        if (userEmail == null) promptBuilder.append("[LƯU Ý]: Khách hàng CHƯA ĐĂNG NHẬP. Không thể thêm vào giỏ hàng.\n\n");
        promptBuilder.append("[CÂU HỎI CỦA KHÁCH HÀNG]\n").append(message);

        log.info(">>> [HYBRID] productId={} | session={} | user={}", productId, sessionId, userEmail);
        doStream(promptBuilder.toString(), message, sessionId, userEmail, finalProductIds, emitter);
    }

    public void streamPureRag(String message, String sessionId, String userEmail, SseEmitter emitter) {
        String historyContext = (sessionId != null) ? chatHistoryService.getHistoryAsContext(sessionId) : "";
        VectorResult vectorResult = retrieveFromVectorDb(message, 30);
        List<ProductDTO> topSelling = getTopSellingIfRequested(message);
        List<Long> finalProductIds = new ArrayList<>(vectorResult.productIds());

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_SPORTBOT).append("\n\n");
        if (!historyContext.isEmpty()) promptBuilder.append(historyContext);
        
        if (!topSelling.isEmpty()) {
            promptBuilder.append("[THÔNG TIN TỪ HỆ THỐNG: SẢN PHẨM BÁN CHẠY NHẤT]\n");
            for(ProductDTO p : topSelling) {
                String priceStr = p.getPrice() != null ? String.valueOf(p.getPrice().longValue()) : "Liên hệ";
                promptBuilder.append("- ").append(p.getName()).append(" (Mã: ").append(p.getProductCode()).append(") - Giá: ").append(priceStr).append("\n");
                if(!finalProductIds.contains(p.getId())) finalProductIds.add(p.getId());
            }
            promptBuilder.append("\n");
        }
        
        if (!vectorResult.context().isEmpty()) promptBuilder.append("[KIẾN THỨC BỔ SUNG]\n").append(vectorResult.context()).append("\n\n");
        if (userEmail == null) promptBuilder.append("[LƯU Ý]: Khách hàng CHƯA ĐĂNG NHẬP. Không thể thêm vào giỏ hàng.\n\n");
        promptBuilder.append("[CÂU HỎI CỦA KHÁCH HÀNG]\n").append(message);

        log.info(">>> [PURE RAG] session={} | user={}", sessionId, userEmail);
        doStream(promptBuilder.toString(), message, sessionId, userEmail, finalProductIds, emitter);
    }

    private void doStream(String fullPrompt, String originalMessage, String sessionId,
                          String userEmail, List<Long> suggestedProductIds, SseEmitter emitter) {

        List<ChatMessage> messages = List.of(userMessage(fullPrompt));
        StringBuilder botResponseBuilder = new StringBuilder();

        if (sessionId != null) {
            chatHistoryService.saveTurn(sessionId, "user", originalMessage);
        }

        streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    botResponseBuilder.append(token);
                    String currentFull = botResponseBuilder.toString();
                    
                    int actionIdx = currentFull.indexOf("%%");
                    if (actionIdx != -1) {
                        int tokenStartIdx = currentFull.length() - token.length();
                        if (actionIdx > tokenStartIdx) {
                            String safePart = token.substring(0, actionIdx - tokenStartIdx);
                            if (!safePart.isEmpty()) {
                                emitter.send(SseEmitter.event().name("token").data(safePart));
                            }
                        }
                        return;
                    }
                    
                    emitter.send(SseEmitter.event().name("token").data(token));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    String fullBotResponse = botResponseBuilder.toString();

                    String cleanResponse = fullBotResponse.replaceAll("%%ACTION:\\{.*?\\}%%", "").trim();
                    if (sessionId != null) {
                        chatHistoryService.saveTurn(sessionId, "bot", cleanResponse);
                    }

                    java.util.regex.Matcher actionMatcher = java.util.regex.Pattern
                            .compile("%%ACTION:(\\{.*?\\})%%", java.util.regex.Pattern.DOTALL)
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

                                emitter.send(SseEmitter.event().name("cart_updated").data("true"));
                                emitter.send(SseEmitter.event().name("action").data("%%ACTION:" + actionJson + "%%"));
                            }
                        } catch (Exception ex) {
                            log.error("Lỗi thực thi Action: {}", ex.getMessage());
                        }
                    }

                    if (!suggestedProductIds.isEmpty()) {
                        List<ChatProductCardDTO> cards = buildProductCards(suggestedProductIds);
                        
                        List<ChatProductCardDTO> filteredCards = new ArrayList<>();
                        String lowerResponse = fullBotResponse.toLowerCase();
                        for (ChatProductCardDTO card : cards) {
                            String lowerName = card.getName().toLowerCase();
                            boolean match = false;
                            
                            if (lowerResponse.contains(lowerName)) {
                                match = true;
                            } else {
                                String[] words = lowerName.split("\\s+");
                                for (String w : words) {
                                    if (w.length() >= 4 && lowerResponse.contains(w)) {
                                        match = true;
                                        break;
                                    }
                                }
                            }
                            if (match) filteredCards.add(card);
                        }

                        if (!filteredCards.isEmpty()) {
                            String cardsJson = objectMapper.writeValueAsString(filteredCards);
                            emitter.send(SseEmitter.event().name("product_cards").data(cardsJson));
                            log.info(">>> [GENERATIVE UI] Gửi {} product cards sau khi lọc.", filteredCards.size());
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

    public com.javaweb.dto.ChatbotResponse chat(String message) {
        VectorResult vectorResult = retrieveFromVectorDb(message, 30);
        List<ProductDTO> topSelling = getTopSellingIfRequested(message);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_SPORTBOT).append("\n\n");
        if (!topSelling.isEmpty()) {
            promptBuilder.append("[THÔNG TIN TỪ HỆ THỐNG: SẢN PHẨM BÁN CHẠY NHẤT]\n");
            for(ProductDTO p : topSelling) {
                String priceStr = p.getPrice() != null ? String.valueOf(p.getPrice().longValue()) : "Liên hệ";
                promptBuilder.append("- ").append(p.getName()).append(" - Giá: ").append(priceStr).append("\n");
            }
            promptBuilder.append("\n");
        }
        if (!vectorResult.context().isEmpty()) promptBuilder.append("[KIẾN THỨC BỔ SUNG]\n").append(vectorResult.context()).append("\n\n");
        promptBuilder.append("[CÂU HỎI CỦA KHÁCH HÀNG]\n").append(message);

        Response<AiMessage> response = chatLanguageModel.generate(userMessage(promptBuilder.toString()));
        return new com.javaweb.dto.ChatbotResponse(response.content().text(), response.content().text());
    }
}
