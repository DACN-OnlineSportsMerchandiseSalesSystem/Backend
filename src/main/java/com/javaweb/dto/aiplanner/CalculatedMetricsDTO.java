package com.javaweb.dto.aiplanner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatedMetricsDTO {
    private Double bmr;                     // BMR (Mifflin-St Jeor)
    private Double pal;                     // Hệ số hoạt động
    private Double tdee;                    // TDEE tiêu hao hàng ngày
    private Double targetCalories;          // Calo mục tiêu (Surplus/Deficit/Maintain)
    private Double calorieAdjustment;       // Mức chênh lệch calo so với TDEE
    private Double targetProteinGrams;      // Gam Protein
    private Double targetCarbsGrams;        // Gam Carbohydrate
    private Double targetFatGrams;          // Gam Fat
    private Double proteinRatioPercent;     // Tỷ lệ % Protein
    private Double carbsRatioPercent;       // Tỷ lệ % Carbs
    private Double fatRatioPercent;         // Tỷ lệ % Fat
    private String formulaUsed;             // "Mifflin-St Jeor Formula (1990)"
}
