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

    private String categoryName;
    private String brandName;

    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;
    private BigDecimal price; // Giá hiển thị đại diện
}