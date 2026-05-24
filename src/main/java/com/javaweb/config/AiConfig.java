package com.javaweb.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private String getResolvedApiKey() {
        String key = geminiApiKey;
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("GEMINI_API_KEY");
        }
        return key != null ? key.trim() : "";
    }

    @Bean
    public ChatLanguageModel geminiChatModel() {
        String apiKey = getResolvedApiKey();
        if (apiKey.isEmpty()) {
            log.warn("=================================================================");
            log.warn("WARNING: Gemini API Key is not configured!");
            log.warn("Please set 'gemini.api.key' in application.properties or set the 'GEMINI_API_KEY' environment variable.");
            log.warn("The Chatbot will return a placeholder error message instead of failing.");
            log.warn("=================================================================");
            return new ChatLanguageModel() {
                @Override
                public Response<AiMessage> generate(List<ChatMessage> messages) {
                    return Response.from(AiMessage.from("Cảnh báo: Gemini API Key chưa được cấu hình. Vui lòng thiết lập gemini.api.key trong application.properties hoặc biến môi trường GEMINI_API_KEY."));
                }
            };
        }
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3-flash-preview")
                .temperature(0.7)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatModel() {
        String apiKey = getResolvedApiKey();
        if (apiKey.isEmpty()) {
            return new StreamingChatLanguageModel() {
                @Override
                public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
                    String warningMsg = "Cảnh báo: Gemini API Key chưa được cấu hình. Vui lòng thiết lập gemini.api.key trong application.properties hoặc biến môi trường GEMINI_API_KEY.";
                    handler.onNext(warningMsg);
                    handler.onComplete(Response.from(AiMessage.from(warningMsg)));
                }
            };
        }
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3-flash-preview")
                .temperature(0.7)
                .build();
    }

    @Bean
    public EmbeddingModel geminiEmbeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // Cố gắng sửa lỗi 405 bằng cách thêm dấu / vào cuối baseUrl
        /*return ChromaEmbeddingStore.builder()
                .baseUrl("http://127.0.0.1:8000/")
                .collectionName("sport_assistant_v1") // Thử một collection name mới
                .build();*/

        // Sử dụng InMemoryEmbeddingStore để không cần chạy ChromaDB
        return new InMemoryEmbeddingStore<>();
    }
}