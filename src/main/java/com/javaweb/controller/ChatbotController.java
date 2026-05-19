package com.javaweb.controller;

import com.javaweb.dto.ChatbotRequest;
import com.javaweb.dto.ChatbotResponse;
import com.javaweb.dto.ChatbotStreamRequest;
import com.javaweb.service.ChatbotService;
import com.javaweb.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin("*")
@Tag(name = "AI Chatbot & RAG", description = "Endpoints for interacting with the AI virtual shopping assistant using hybrid RAG, function calling, and Server-Sent Events (SSE) streaming")
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
    @Operation(summary = "Synchronous AI chat greeting", description = "Get a fast synchronous response from the AI model (suitable for generic welcome greetings).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully received chatbot response")
    })
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
    @Operation(summary = "Asynchronous SSE chat stream (Hybrid RAG)", description = "Initiate an SSE stream connection with the AI assistant. Supports catalog queries, function calling, product cards rendering, and shopping advice.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully established Server-Sent Events (SSE) stream emitter")
    })
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
    @Operation(summary = "Clear chat session history", description = "Purge all conversational messages stored in memory/Redis associated with a given session UUID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session memory purged successfully")
    })
    public ResponseEntity<Void> clearHistory(
            @Parameter(description = "UUID session identifier", example = "a2c3-dfd4-f3c4", required = true)
            @PathVariable String sessionId) {
        chatHistoryService.clearHistory(sessionId);
        return ResponseEntity.ok().build();
    }
}
