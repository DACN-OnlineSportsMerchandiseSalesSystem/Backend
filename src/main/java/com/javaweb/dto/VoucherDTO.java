package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class VoucherDTO {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private Integer usedCount;
    private Date expiryDate;
    private Date createdAt;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
}
