package com.javaweb.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${chroma.base-url:http://127.0.0.1:8000/}")
    private String chromaBaseUrl;

    /**
     * Hàm kiểm tra (If-Else) xem AI có được kích hoạt hay không.
     * AI sẽ tự động kích hoạt nếu tìm thấy khóa GEMINI_API_KEY (trong cấu hình hoặc
     * biến môi trường).
     */
    private boolean isAiEnabled() {
        String key = geminiApiKey;
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("GEMINI_API_KEY");
        }
        return key != null && !key.trim().isEmpty();
    }

    private String getEffectiveApiKey() {
        String key = geminiApiKey;
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("GEMINI_API_KEY");
        }
        return key != null ? key.trim() : "";
    }

    // ==========================================
    // 1. GEMINI CHAT MODEL (IF-ELSE CONDITIONAL)
    // ==========================================
    @Bean
    public ChatLanguageModel geminiChatModel() {
        if (isAiEnabled()) {
            // IF: Có API Key -> Chạy AI thật của Gemini
            return GoogleAiGeminiChatModel.builder()
                    .apiKey(getEffectiveApiKey())
                    .modelName("gemini-3.1-flash-lite")
                    .temperature(0.7)
                    .build();
        } else {
            // ELSE: Không có API Key -> Trả về Mock an toàn để tránh sập
            return new ChatLanguageModel() {
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages) {
                    return Response.from(AiMessage.from("AI is currently disabled (Gemini API Key is missing)."));
                }
            };
        }
    }

    // ==========================================
    // 2. STREAMING CHAT MODEL (IF-ELSE CONDITIONAL)
    // ==========================================
    @Bean
    public StreamingChatLanguageModel streamingChatModel() {
        if (isAiEnabled()) {
            // IF: Có API Key -> Chạy AI thật dạng Streaming
            return GoogleAiGeminiStreamingChatModel.builder()
                    .apiKey(getEffectiveApiKey())
                    .modelName("gemini-3.1-flash-lite")
                    .temperature(0.7)
                    .build();
        } else {
            // ELSE: Không có API Key -> Trả về Mock an toàn dạng Streaming
            return new StreamingChatLanguageModel() {
                @Override
                public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
                    String response = "AI is currently disabled (Gemini API Key is missing).";
                    handler.onNext(response);
                    handler.onComplete(Response.from(AiMessage.from(response)));
                }
            };
        }
    }

    // ==========================================
    // 3. EMBEDDING MODEL (IF-ELSE CONDITIONAL)
    // ==========================================
    @Bean
    public EmbeddingModel geminiEmbeddingModel() {
        if (isAiEnabled()) {
            // IF: Có cấu hình AI -> Sử dụng Cloud Embedding Model của Google (không tốn
            // CPU/RAM local)
            return GoogleAiEmbeddingModel.builder()
                    .apiKey(getEffectiveApiKey())
                    .modelName("gemini-embedding-001")
                    .outputDimensionality(384)
                    .build();
        } else {
            // ELSE: Chạy Mock giả lập offline để tránh lỗi UnsatisfiedLinkError trên Docker
            // Alpine
            return new EmbeddingModel() {
                @Override
                public Response<Embedding> embed(String text) {
                    return Response.from(new Embedding(new float[384]));
                }

                @Override
                public Response<Embedding> embed(TextSegment textSegment) {
                    return Response.from(new Embedding(new float[384]));
                }

                @Override
                public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
                    List<Embedding> list = new ArrayList<>();
                    for (TextSegment ignored : textSegments) {
                        list.add(new Embedding(new float[384]));
                    }
                    return Response.from(list);
                }
            };
        }
    }

    // ==========================================
    // 4. EMBEDDING STORE (IF-ELSE CONDITIONAL)
    // ==========================================
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        if (isAiEnabled()) {
            // IF: Có cấu hình AI -> Kết nối vào cơ sở dữ liệu vector Chroma DB thật
            return ChromaEmbeddingStore.builder()
                    .baseUrl(chromaBaseUrl)
                    .collectionName("sport_assistant_v2")
                    .build();
        } else {
            // ELSE: Chạy InMemory Store offline lưu trên RAM máy local
            return new InMemoryEmbeddingStore<>();
        }
    }
}
