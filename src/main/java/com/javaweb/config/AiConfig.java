package com.javaweb.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Bean
    public ChatLanguageModel geminiChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey.trim())
                .modelName("gemini-3-flash-preview")
                .temperature(0.7)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatModel() {
        // Sử dụng StreamingChatLanguageModel chuyên biệt cho việc phát luồng
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(geminiApiKey.trim())
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
