package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnItemRequestDTO {
    private Long orderItemId;
    private Integer quantity;
    private String imageProof;
}
