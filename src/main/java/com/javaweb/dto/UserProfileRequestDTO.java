package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequestDTO {
    private String sports;
    private String sportLevel;
    private String wristStrength;
    private String preferredBudget;
    private String injuries;
}
