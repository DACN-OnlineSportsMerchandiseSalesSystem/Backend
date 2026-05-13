package com.javaweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin tối thiểu của sản phẩm để hiển thị
 * dưới dạng Product Card trực tiếp trong giao diện Chat (Generative UI).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatProductCardDTO {
    private Long id;
    private String name;
    private String slug;
    private String brandName;
    private BigDecimal price;
    private String imageUrl;   // Ảnh thumbnail
}
