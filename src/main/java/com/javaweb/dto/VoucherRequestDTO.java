package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class VoucherRequestDTO {
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private Date expiryDate;
    private Long categoryId;
    private Long brandId;
    private Long sportId;
}
