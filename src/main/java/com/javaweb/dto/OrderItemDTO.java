package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemDTO {
    private Long id;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private Long productId;
    private Long productVariantId; // Trích xuất ID trực tiếp thay vì bế luôn cả một Entity ProductVariant vào
}
