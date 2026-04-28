package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {
    private Long id;
    private String title;
    private String comment;
    private Integer rating;
    private java.util.Date createdAt;
    private String adminReply;
    private java.util.Date repliedAt;
    private String userName; // Tên hiển thị người đánh giá
    private Long productId;
}
