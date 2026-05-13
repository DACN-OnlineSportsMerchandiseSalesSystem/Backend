package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class StorePolicyDTO {
    private Long id;
    private String policyKey;
    private String title;
    private String content;
    private String category;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime updatedAt;
}
