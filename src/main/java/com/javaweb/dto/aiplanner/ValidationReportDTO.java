package com.javaweb.dto.aiplanner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationReportDTO {
    private Double calculatedMacroCalories;  // 4*P + 4*C + 9*F
    private Double targetCalories;           // Calo mục tiêu
    private Double thermodynamicDiffPercent; // % Sai số nhiệt động học
    private Boolean isThermodynamicallyValid;// <= 5%
    private Map<String, Integer> weeklyVolumeSets; // Tổng sets theo nhóm cơ (ngưỡng 10-20)
    private Boolean isVolumeOptimal;         // Volume có nằm trong ngưỡng kích thích tối ưu không
    private List<String> safetyNotes;        // Cảnh báo an toàn chấn thương / dị ứng
    private List<String> optimizationNotes;  // Lời khuyên tối ưu
}
