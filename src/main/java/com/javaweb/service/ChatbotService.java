package com.javaweb.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final SportAssistant assistant;
    private final SportAssistant assistantStreaming;

    interface SportAssistant {
        @SystemMessage({
            "Bạn là SportBot, trợ lý tư vấn khách hàng cho SportZone.",
            "Nhiệm vụ: Tư vấn sản phẩm, chính sách và kiến thức thể thao.",
            "QUY ĐỊNH TRẢ LỜI:",
            "- Trả lời tự nhiên, thân thiện, ngắn gọn, có sử dụng emoji phù hợp.",
            "- TUYỆT ĐỐI GIỮ NGUYÊN các từ tiếng Anh, tên thương hiệu, tên sản phẩm (Ví dụ: Nike Air Zoom Pegasus 40). KHÔNG ĐƯỢC tự ý phiên âm tiếng Anh sang tiếng Việt.",
            "- Viết đúng chính tả chữ SportZone và SportBot.",
            "Ví dụ:",
            "Chào bạn, mình là SportBot từ SportZone. Bạn cần tìm hiểu về đôi giày Nike Air Zoom Pegasus 40 đúng không ạ? 😊"
        })
        String chat(String userMessage);

        TokenStream chatStream(String userMessage);
    }

    public ChatbotService(ChatLanguageModel chatLanguageModel, 
                          StreamingChatLanguageModel streamingChatLanguageModel,
                          EmbeddingModel embeddingModel, 
                          EmbeddingStore<TextSegment> embeddingStore) {
        
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

        this.assistantStreaming = AiServices.builder(SportAssistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }

    public TokenStream streamChat(String message) {
        return assistantStreaming.chatStream(message);
    }

    public com.javaweb.dto.ChatbotResponse chat(String message) {
        String raw = assistant.chat(message);
        return new com.javaweb.dto.ChatbotResponse(raw, raw);
    }
}
