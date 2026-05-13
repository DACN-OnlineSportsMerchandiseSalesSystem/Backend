package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDTO {
    private Long id;
    private String name;
    private String detail;
    private String imageUrl;
    private String status;
    private Integer discount;
    private Integer rating;
}
