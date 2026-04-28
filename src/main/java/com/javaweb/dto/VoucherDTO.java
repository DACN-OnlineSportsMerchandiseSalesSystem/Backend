package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class VoucherDTO {
    private String code;
    private BigDecimal discountAmount;
}
