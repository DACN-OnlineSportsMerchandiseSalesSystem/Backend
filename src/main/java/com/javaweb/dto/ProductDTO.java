package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

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
}