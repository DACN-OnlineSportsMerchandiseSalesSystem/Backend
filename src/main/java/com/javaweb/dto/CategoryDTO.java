package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String status;
    private Integer rating;
    private Long parentId;
    private String parentName;
}
