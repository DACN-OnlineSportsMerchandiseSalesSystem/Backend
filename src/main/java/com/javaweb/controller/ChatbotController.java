package com.javaweb.controller;

import com.javaweb.dto.ChatbotRequest;
import com.javaweb.dto.ChatbotResponse;
import com.javaweb.dto.ChatbotStreamRequest;
import com.javaweb.service.ChatbotService;
import com.javaweb.service.ChatHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin("*")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatHistoryService chatHistoryService;

    public ChatbotController(ChatbotService chatbotService, ChatHistoryService chatHistoryService) {
        this.chatbotService = chatbotService;
        this.chatHistoryService = chatHistoryService;
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
     * Phương thức phát luồng (POST) — RAG Hybrid hoặc Pure RAG tuỳ productId.
     * Tích hợp: Redis Memory + Function Calling + Generative UI Product Cards.
     *
     * Body: { "message": "...", "productId": 1, "sessionId": "uuid", "userEmail": "..." }
     */
    @PostMapping("/stream")
    public SseEmitter chatStream(
            @RequestBody ChatbotStreamRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SseEmitter emitter = new SseEmitter(90000L); // 90 giây timeout

        String message   = request.getMessage();
        Long productId   = request.getProductId();
        String sessionId = request.getSessionId();

        // Ưu tiên lấy email từ JWT (nếu đã login), nếu không thì từ request body
        String userEmail = (userDetails != null) ? userDetails.getUsername() : request.getUserEmail();

        System.out.printf(">>> [STREAM] msg='%s' | productId=%s | session=%s | user=%s%n",
                message, productId, sessionId, userEmail);

        if (productId != null) {
            chatbotService.streamHybrid(message, productId, sessionId, userEmail, emitter);
        } else {
            chatbotService.streamPureRag(message, sessionId, userEmail, emitter);
        }

        return emitter;
    }

    /**
     * Xóa lịch sử chat của một phiên (khi user đóng chat hoặc bắt đầu lại)
     */
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String sessionId) {
        chatHistoryService.clearHistory(sessionId);
        return ResponseEntity.ok().build();
    }
}
