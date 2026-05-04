package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductVariantDTO {
    private Long id;
    private String skuCode;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer stockQuantity;
}
