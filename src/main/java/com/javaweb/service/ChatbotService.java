package com.javaweb.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final SportAssistant assistant;

    interface SportAssistant {
        @SystemMessage({
            "Bạn là SportBot, trợ lý tư vấn khách hàng cho SportZone.",
            "Nhiệm vụ: Tư vấn sản phẩm, chính sách và kiến thức thể thao.",
            "QUY ĐỊNH TRẢ LỜI: Bạn PHẢI trả lời theo cấu trúc sau:",
            "[TEXT]: Nội dung hiển thị cho khách (đầy đủ, chuyên nghiệp, có emoji).",
            "[VOICE]: Nội dung dành cho giọng đọc (Việt hóa 100%, không dùng ký tự đặc biệt).",
            "QUY TẮC PHIÊN ÂM TÊN RIÊNG:",
            "- SportZone đọc là: sờ pót dôn",
            "- SportBot đọc là: sờ pót bót",
            "Ví dụ:",
            "[TEXT]: Chào bạn, mình là SportBot từ SportZone. Bạn cần mua gì?",
            "[VOICE]: chào bạn, mình là sờ pót bót từ sờ pót dôn bạn cần mua gì?"
        })
        String chat(String userMessage);
    }

    public ChatbotService(ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();

        this.assistant = AiServices.builder(SportAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }

    public com.javaweb.dto.ChatbotResponse chat(String message) {
        String raw = assistant.chat(message);
        String text = "";
        String voice = "";
        
        try {
            if (raw.contains("[TEXT]:") && raw.contains("[VOICE]:")) {
                text = raw.substring(raw.indexOf("[TEXT]:") + 7, raw.indexOf("[VOICE]:")).trim();
                voice = raw.substring(raw.indexOf("[VOICE]:") + 8).trim();
            } else {
                text = raw;
                voice = raw;
            }
        } catch (Exception e) {
            text = raw;
            voice = raw;
        }
        
        return new com.javaweb.dto.ChatbotResponse(text, voice);
    }
}
