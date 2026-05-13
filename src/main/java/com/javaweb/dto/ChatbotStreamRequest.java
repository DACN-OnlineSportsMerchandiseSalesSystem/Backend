package com.javaweb.dto;

import lombok.Data;

@Data
public class ChatbotStreamRequest {
    private String message;
    private Long productId;    // null nếu không ở trang sản phẩm (Pure RAG)
    private String sessionId;  // UUID từ Frontend — dùng để tra lịch sử Redis
    private String userEmail;  // null nếu chưa đăng nhập
}
