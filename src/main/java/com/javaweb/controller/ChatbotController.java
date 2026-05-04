package com.javaweb.controller;

import com.javaweb.dto.ChatbotRequest;
import com.javaweb.dto.ChatbotResponse;
import com.javaweb.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatbotResponse> chat(@RequestBody ChatbotRequest request) {
        try {
            System.out.println(">>> Nhận tin nhắn AI: " + request.getMessage());
            ChatbotResponse response = chatbotService.chat(request.getMessage());
            System.out.println(">>> Bot trả lời: " + response.getResponse());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("!!! Lỗi Chatbot AI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ChatbotResponse("Hệ thống AI đang gặp sự cố kỹ thuật.", "hệ thống gặp sự cố"));
        }
    }
}
