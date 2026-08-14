package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ComboItemDTO {
    private Long id;
    private Long productVariantId;
    private Long productId;
    private String productName;
    private String skuCode;
    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal price;
}
