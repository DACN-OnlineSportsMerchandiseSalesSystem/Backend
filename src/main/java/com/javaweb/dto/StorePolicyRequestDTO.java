package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorePolicyRequestDTO {
    private String policyKey;
    private String title;
    private String content;
    private String category;
    private Boolean isActive;
    private Integer displayOrder;
}
