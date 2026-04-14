package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItemDTO {
    private Long id;
    private Long productVariantId;
    // Dữ liệu rút gọn từ ProductVariant để hiện thị giao diện
    private String imageUrl;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
}
