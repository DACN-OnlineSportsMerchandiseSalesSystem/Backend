package com.javaweb.dto.aiplanner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPlannerRefineRequestDTO {
    private Long planId;                       // ID snapshot hiện tại (nếu có)
    private CalculatedMetricsDTO metrics;       // Metrics neo sinh học hiện tại
    private Map<String, Object> currentWorkoutPlan; // Kế hoạch tập hiện tại
    private Map<String, Object> currentMealPlan;    // Kế hoạch ăn hiện tại
    private String refinementPrompt;           // Lệnh tinh chỉnh (VD: "Đổi ức gà sang thịt bò", "Dời thứ 4 sang thứ 5")
    private String targetType;                 // "workout", "meal", hoặc "both"
    private String userEmail;                  // Email người dùng
}
