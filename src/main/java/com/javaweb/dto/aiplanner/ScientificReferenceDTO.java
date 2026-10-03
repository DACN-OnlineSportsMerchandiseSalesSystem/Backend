package com.javaweb.dto.aiplanner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScientificReferenceDTO {
    /**
     * Lĩnh vực khoa học: BMR & TDEE, Phân bổ Macronutrient, Volume & Progressive Overload, Hydration & Recovery
     */
    private String domain;

    /**
     * Nguyên lý / Định luật khoa học áp dụng
     */
    private String principle;

    /**
     * Tổ chức / Tạp chí khoa học phát hành (ACSM, ISSN, WHO, AJCN)
     */
    private String source;

    /**
     * Năm công bố
     */
    private String year;

    /**
     * Trích dẫn chuẩn khoa học (Citation)
     */
    private String citation;

    /**
     * Cách thức áp dụng cụ thể vào kế hoạch này của người dùng
     */
    @JsonProperty("applicationInPlan")
    @JsonAlias({"application_in_plan", "applicationInPlan"})
    private String applicationInPlan;

    @JsonProperty("application_in_plan")
    public String getApplication_in_plan() {
        return this.applicationInPlan;
    }

    public void setApplication_in_plan(String application_in_plan) {
        this.applicationInPlan = application_in_plan;
    }
}
