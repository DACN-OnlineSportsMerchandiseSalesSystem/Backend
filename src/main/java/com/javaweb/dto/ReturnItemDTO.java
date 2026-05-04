package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnItemDTO {
    private Long id;
    private Long orderItemId;
    private String productName;
    private Integer quantity;
    private String imageProof;
}
