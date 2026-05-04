package com.javaweb.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // Trong AiConfig.java
    @Bean
    public ChatLanguageModel geminiChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey.trim()) // Xóa khoảng trắng nếu có
                .modelName("gemini-2.5-flash-lite") // Dùng ID đầy đủ
                .temperature(0.7)
                .build();
    }

    @Bean
    public EmbeddingModel geminiEmbeddingModel() {
        // Sử dụng mô hình Local chạy trực tiếp trong bộ nhớ (không cần cấu hình API
        // Key)
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // Dùng bộ nhớ tạm để lưu Vector. Thích hợp cho môi trường Dev.
        // Bạn có thể đổi sang ChromaDB hoặc Pinecone dễ dàng sau này bằng cách thay
        // implementation này.
        return new InMemoryEmbeddingStore<>();
    }
}
