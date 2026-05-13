package com.javaweb.dto;

import com.javaweb.enums.DiscountScope;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class DiscountRequestDTO {
    private String name;
    private Integer discountPercent;
    private DiscountScope scope;
    private Long categoryId;  // Bắt buộc khi scope = CATEGORY
    private Long brandId;     // Bắt buộc khi scope = BRAND
    private Date startDate;
    private Date endDate;
    private Boolean isActive;
}
