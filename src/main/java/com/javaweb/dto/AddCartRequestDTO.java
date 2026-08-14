package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AddCartRequestDTO {
    private Long productVariantId;
    private Integer quantity;
    private List<Long> childVariantIds;
}
