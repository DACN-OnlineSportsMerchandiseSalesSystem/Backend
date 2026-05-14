package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductDTO {
    private Long id;
    private String name;
    private String productCode;
    private String searchTag;   
    private String description;
    private String slug;        
    private String status;     
    private Integer discount;
    private Double rating;
    private Integer reviewCount;

    private List<Long> categoryIds;
    private List<String> categoryNames;
    private String brandName;
    private Long brandId;

    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;
    private BigDecimal price; // Giá hiển thị đại diện
    private BigDecimal originalPrice;
}