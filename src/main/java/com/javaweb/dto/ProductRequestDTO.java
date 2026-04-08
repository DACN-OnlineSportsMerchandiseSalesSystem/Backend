package com.javaweb.dto;

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
    
    // Nhận ID từ Frontend thay vì nhận chuỗi tên
    private Long categoryId;
    private Long brandId;
}
