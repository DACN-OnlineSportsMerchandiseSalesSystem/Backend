package com.javaweb.controller;

import com.javaweb.dto.ChatbotRequest;
import com.javaweb.dto.ChatbotResponse;
import com.javaweb.dto.ChatbotStreamRequest;
import com.javaweb.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin("*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Phương thức đồng bộ (POST) — Dùng cho lời chào nhanh
     */
    @PostMapping
    public ResponseEntity<ChatbotResponse> chat(@RequestBody ChatbotRequest request) {
        try {
            ChatbotResponse response = chatbotService.chat(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ChatbotResponse("Lỗi hệ thống", "lỗi hệ thống"));
        }
    }

    /**
     * Phương thức phát luồng (POST) — RAG Hybrid hoặc Pure RAG tuỳ productId
     *
     * Body: { "message": "...", "productId": 1 }
     * - Có productId  → Trường hợp A: Hybrid RAG (sản phẩm + kho tri thức)
     * - Không có      → Trường hợp B: Pure RAG (chỉ kho tri thức)
     */
    @PostMapping("/stream")
    public SseEmitter chatStream(@RequestBody ChatbotStreamRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);

        String message = request.getMessage();
        Long productId = request.getProductId();

        System.out.printf(">>> [STREAM] message='%s' | productId=%s%n", message, productId);

        if (productId != null) {
            // Trường hợp A: Trang Chi tiết Sản phẩm → Hybrid RAG
            chatbotService.streamHybrid(message, productId, emitter);
        } else {
            // Trường hợp B: Ngoài trang Sản phẩm → Pure RAG
            chatbotService.streamPureRag(message, emitter);
        }

        return emitter;
    }
}
