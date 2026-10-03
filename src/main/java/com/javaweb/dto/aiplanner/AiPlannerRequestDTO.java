package com.javaweb.dto.aiplanner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPlannerRequestDTO {
    private Double height;              // Chiều cao (cm)
    private Double weight;              // Cân nặng (kg)
    private Integer age;                // Tuổi
    private String gender;              // "male" hoặc "female"
    private String activityLevel;       // "sedentary", "light", "moderate", "active", "very_active"
    private String goal;                // "bulk", "cut", "maintain", "endurance"
    private String location;            // "gym" hoặc "home"
    private String injuries;            // Tiền sử chấn thương (ví dụ: "đau khớp gối", "lưng dưới")
    private String dietaryPreferences;  // Dị ứng / Ưu tiên ẩm thực (ví dụ: "không sữa", "món Việt")
    private Integer daysPerWeek;        // Số buổi tập/tuần (3 - 6)
    private String customNotes;         // Ghi chú bổ sung
    private String userEmail;           // Email người dùng (nếu có)
}
