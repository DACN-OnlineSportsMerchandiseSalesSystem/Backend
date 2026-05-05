package com.javaweb.dto;

import lombok.Data;

@Data
public class ChatbotStreamRequest {
    private String message;
    private Long productId; // null nếu không ở trang sản phẩm (Pure RAG)
}
