package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartRequestDTO {
    private String name;
    private Boolean isDefault;
}
