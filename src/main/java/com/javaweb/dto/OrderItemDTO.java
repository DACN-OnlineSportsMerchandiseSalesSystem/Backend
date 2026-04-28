package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemDTO {
    private Long id;
    private String imageUrl;
    private BigDecimal priceAtPurchase;
    private BigDecimal discountAmount;
    private int quantity;
    private Long productVariantId;
    
    // Thêm các trường hiển thị chi tiết sản phẩm cho Frontend đỡ phải gọi API nhiều lần
    private String productName;
    private String size;
    private String color;
}
