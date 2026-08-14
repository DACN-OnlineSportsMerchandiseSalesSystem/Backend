package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartRequestDTO {
    private String name;
    @com.fasterxml.jackson.annotation.JsonProperty("isDefault")
    private Boolean isDefault;
}
