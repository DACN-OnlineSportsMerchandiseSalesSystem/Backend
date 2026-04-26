package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {
    private Long id;
    private String content;
    private Integer rating;
    private String userName; // Tên hiển thị người đánh giá
    private Long productId;
}
