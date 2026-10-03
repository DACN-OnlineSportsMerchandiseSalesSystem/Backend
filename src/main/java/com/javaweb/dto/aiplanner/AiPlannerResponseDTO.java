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
public class AiPlannerResponseDTO {
    private CalculatedMetricsDTO metrics;
    private Map<String, Object> workoutPlan;
    private Map<String, Object> mealPlan;
    private ValidationReportDTO validationReport;
    private String planTitle;
    private Long savedPlanId;
    private Integer version;

    /**
     * HẠNG MỤC 1: Danh sách tham chiếu nguyên lý khoa học thể thao & dinh dưỡng y khoa (ACSM, ISSN, WHO, Mifflin-St Jeor)
     */
    private List<ScientificReferenceDTO> scientificReferences;

    /**
     * HẠNG MỤC 1: Miễn trừ trách nhiệm pháp lý bắt buộc (Legal Disclaimer)
     */
    private String legalDisclaimer;
}
