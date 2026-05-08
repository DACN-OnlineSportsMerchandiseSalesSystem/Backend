package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SportDTO {
    private Long id;
    private String name;
    private String slug;
    private Integer rating;
    private String status;
    private Integer discount;
}
