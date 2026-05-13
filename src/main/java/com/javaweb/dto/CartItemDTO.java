package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItemDTO {
    private Long id;
    private Long productVariantId;
    private Long productId;      // Cần ID sản phẩm gốc để chuyển trang
    private String productName;  // Tên sản phẩm
    private String variantInfo;  // Thông tin Size/Màu
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
}
