package com.javaweb.controller;

import com.javaweb.dto.ChatbotRequest;
import com.javaweb.dto.ChatbotResponse;
import com.javaweb.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin("*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Phương thức đồng bộ (POST) - Dùng cho lời chào nhanh
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
     * Phương thức phát luồng (GET) - Dùng cho hội thoại Cuốn chiếu
     */
    @GetMapping("/stream")
    public SseEmitter chatStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60000L);
        
        System.out.println(">>> [STREAM] Nhận tin nhắn AI: " + message);

        chatbotService.streamChat(message)
            .onNext(token -> {
                try {
                    emitter.send(token);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            })
            .onComplete(response -> {
                emitter.complete();
            })
            .onError(error -> {
                emitter.completeWithError(error);
            })
            .start();

        return emitter;
    }
}
