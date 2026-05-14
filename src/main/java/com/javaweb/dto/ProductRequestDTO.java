package com.javaweb.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDTO {
    private String name;
    private String productCode;
    private String searchTag;
    private String description;
    private String slug;
    private String status;

    private Integer discount;
    private BigDecimal originalPrice;

    // Nhận ID từ Frontend thay vì nhận chuỗi tên
    private Long productVariantId;
    private List<Long> categoryIds;
    private Long brandId;
    private String imageUrl;
    private List<String> sizes;
    private List<String> colors;
    private Integer stockQuantity;
}
