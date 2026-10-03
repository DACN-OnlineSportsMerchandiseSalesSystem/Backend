package com.javaweb.service;

import com.javaweb.dto.aiplanner.AiPlannerRefineRequestDTO;
import com.javaweb.dto.aiplanner.AiPlannerRequestDTO;
import com.javaweb.dto.aiplanner.AiPlannerResponseDTO;
import com.javaweb.dto.aiplanner.CalculatedMetricsDTO;
import com.javaweb.entity.AiPlanSnapshot;

import java.util.List;

public interface AiPlannerService {

    /**
     * Module 1: Tính toán chỉ số sinh học mỏ neo chuẩn toán học xác định (Mifflin-St Jeor)
     */
    CalculatedMetricsDTO calculateDeterministicMetrics(AiPlannerRequestDTO req);

    /**
     * Module 1-4: Tạo kế hoạch tập luyện và thực đơn hoàn chỉnh dựa trên 6 module khoa học
     */
    AiPlannerResponseDTO generatePlan(AiPlannerRequestDTO request);

    /**
     * Module 5: Tinh chỉnh vi sai (Delta Update) cho một phần của kế hoạch mà không phá vỡ cân bằng calo
     */
    AiPlannerResponseDTO refinePlan(AiPlannerRefineRequestDTO request);

    /**
     * Module 6: Lưu snapshot kế hoạch vào Database kèm quản lý phiên bản (Versioning)
     */
    AiPlanSnapshot saveSnapshot(String title, String planType, AiPlannerResponseDTO planData, String userEmail);

    /**
     * Module 6: Lấy danh sách các kế hoạch đã lưu của người dùng
     */
    List<AiPlanSnapshot> getSavedPlans(String userEmail);

    /**
     * Module 6: Lấy chi tiết một kế hoạch theo ID
     */
    AiPlanSnapshot getPlanById(Long id, String userEmail);

    /**
     * Module 6: Đặt một kế hoạch làm trạng thái Kích hoạt (Active)
     */
    void setActivePlan(Long id, String userEmail);

    /**
     * Module 6: Xóa một kế hoạch đã lưu
     */
    void deletePlan(Long id, String userEmail);

    /**
     * Module 6: Xuất lịch tập sang định dạng chuẩn iCalendar (.ics)
     */
    String exportIcs(Long id, String userEmail);
}
