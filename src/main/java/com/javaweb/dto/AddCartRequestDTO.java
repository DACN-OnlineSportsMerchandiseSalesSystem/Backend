package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddCartRequestDTO {
    private Long productVariantId;
    private BigDecimal quantity;
}
