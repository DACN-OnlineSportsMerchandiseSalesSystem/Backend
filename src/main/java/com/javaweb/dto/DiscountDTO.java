package com.javaweb.dto;

import com.javaweb.enums.DiscountScope;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class DiscountDTO {
    private Long id;
    private String name;
    private Integer discountPercent;
    private DiscountScope scope;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Date startDate;
    private Date endDate;
    private Boolean isActive;
    private Date createdAt;
}
