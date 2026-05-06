package com.javaweb.service;

import com.javaweb.entity.Product;
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
import java.util.List;
import java.util.Optional;
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
        "- KHÔNG bắt đầu câu trả lời bằng 'Dựa trên thông tin...' hay 'Theo ngữ cảnh...'.\n" +
        "- Có thể dùng emoji phù hợp để thân thiện hơn.";

    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ProductRepository productRepository;

    public ChatbotService(ChatLanguageModel chatLanguageModel,
                          StreamingChatLanguageModel streamingChatLanguageModel,
                          EmbeddingModel embeddingModel,
                          EmbeddingStore<TextSegment> embeddingStore,
                          ProductRepository productRepository) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.productRepository = productRepository;
    }

    // =====================================================
    // Hàm truy vấn Vector DB (dùng chung cho cả 2 luồng)
    // =====================================================
    private String retrieveFromVectorDb(String question, int maxResults) {
        try {
            Embedding queryEmbedding = embeddingModel.embed(question).content();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, maxResults, 0.45);

            if (matches == null || matches.isEmpty()) return "";

            return matches.stream()
                    .map(m -> m.embedded().text())
                    .collect(Collectors.joining("\n---\n"));
        } catch (Exception e) {
            log.warn("Không thể truy vấn Vector DB: {}", e.getMessage());
            return "";
        }
    }

    // =====================================================
    // Lấy thông tin sản phẩm từ DB (dùng cho Trường hợp A)
    // =====================================================
    private String buildProductContext(Long productId) {
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty()) return "";

        Product p = optProduct.get();
        StringBuilder sb = new StringBuilder();
        sb.append("Sản phẩm khách đang xem:\n");
        sb.append("- Tên: ").append(p.getName()).append("\n");
        sb.append("- Thương hiệu: ").append(p.getBrand() != null ? p.getBrand().getName() : "N/A").append("\n");
        sb.append("- Danh mục: ").append(p.getCategory() != null ? p.getCategory().getName() : "N/A").append("\n");
        sb.append("- Mã SP: ").append(p.getProductCode()).append("\n");
        sb.append("- Mô tả: ").append(p.getDescription() != null ? p.getDescription() : "Không có").append("\n");

        if (p.getProductVariants() != null && !p.getProductVariants().isEmpty()) {
            sb.append("- Phiên bản có sẵn trong kho:\n");
            p.getProductVariants().forEach(v ->
                sb.append("  • Size ").append(v.getSize())
                  .append(", Màu ").append(v.getColor())
                  .append(", Giá: ").append(v.getPrice()).append("đ")
                  .append(", Tồn: ").append(v.getStockQuantity()).append("\n")
            );
        }
        return sb.toString();
    }

    // =====================================================
    // TRƯỜNG HỢP A: Hybrid RAG — Trang Chi tiết Sản phẩm
    // =====================================================
    public void streamHybrid(String message, Long productId, SseEmitter emitter) {
        String productContext = buildProductContext(productId);
        String ragContext = retrieveFromVectorDb(message, 3);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_SPORTBOT).append("\n\n");

        if (!productContext.isEmpty()) {
            promptBuilder.append("[THÔNG TIN SẢN PHẨM]\n").append(productContext).append("\n");
        }
        if (!ragContext.isEmpty()) {
            promptBuilder.append("[KIẾN THỨC BỔ SUNG TỪ KHO DỮ LIỆU]\n").append(ragContext).append("\n");
        }
        promptBuilder.append("\n[CÂU HỎI CỦA KHÁCH HÀNG]\n").append(message);

        log.info(">>> [HYBRID] productId={}", productId);
        doStream(promptBuilder.toString(), emitter);
    }

    // =====================================================
    // TRƯỜNG HỢP B: Pure RAG — Ngoài trang Sản phẩm
    // =====================================================
    public void streamPureRag(String message, SseEmitter emitter) {
        String ragContext = retrieveFromVectorDb(message, 4);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_SPORTBOT).append("\n\n");

        if (!ragContext.isEmpty()) {
            promptBuilder.append("[KIẾN THỨC TỪ KHO DỮ LIỆU SPORTZONE]\n").append(ragContext).append("\n\n");
        }
        promptBuilder.append("[CÂU HỎI CỦA KHÁCH HÀNG]\n").append(message);

        log.info(">>> [PURE RAG]");
        doStream(promptBuilder.toString(), emitter);
    }

    // =====================================================
    // Hàm Streaming chung — API đúng cho LangChain4j 0.36.x
    // =====================================================
    private void doStream(String fullPrompt, SseEmitter emitter) {
        List<ChatMessage> messages = List.of(userMessage(fullPrompt));

        streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                log.error("Stream error: {}", error.getMessage());
                emitter.completeWithError(error);
            }
        });
    }

    // =====================================================
    // Phương thức đồng bộ — API đúng cho LangChain4j 0.36.x
    // =====================================================
    public com.javaweb.dto.ChatbotResponse chat(String message) {
        String ragContext = retrieveFromVectorDb(message, 3);
        String prompt = SYSTEM_SPORTBOT + "\n\n" +
            (ragContext.isEmpty() ? "" : "[KIẾN THỨC]\n" + ragContext + "\n\n") +
            "[CÂU HỎI]\n" + message;

        // LangChain4j 0.36.x: dùng generate(String) cho sync chat
        String raw = chatLanguageModel.generate(prompt);
        return new com.javaweb.dto.ChatbotResponse(raw, raw);
    }
}
