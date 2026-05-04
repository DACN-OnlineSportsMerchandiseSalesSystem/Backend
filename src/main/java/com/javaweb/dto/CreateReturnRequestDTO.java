package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateReturnRequestDTO {
    private Long orderId;
    private String reason;
    private List<ReturnItemRequestDTO> items;
}
