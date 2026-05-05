package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private Boolean isThumbnail;
}
