package com.javaweb.controller;

import com.javaweb.dto.*;
import com.javaweb.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    // Lấy dữ liệu kho để vẽ biểu đồ
    @GetMapping("/daily")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DailyStatisticDTO>> getDailyStatistics(
            @RequestParam(required = false, defaultValue = "0") int month,
            @RequestParam(required = false, defaultValue = "0") int year) {
        
        return ResponseEntity.ok(statisticService.getDailyStatistics(month, year));
    }

    // Lấy top sản phẩm bán chạy nhất
    @GetMapping("/top-selling")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProducts(
            @RequestParam(required = false, defaultValue = "0") int month,
            @RequestParam(required = false, defaultValue = "0") int year,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        
        return ResponseEntity.ok(statisticService.getTopSellingProducts(month, year, limit));
    }

    // Nút bấm ma thuật: Kích hoạt ETL gom dữ liệu ngay lập tức (Dùng lúc Demo đồ án)
    @PostMapping("/trigger-sync")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> triggerSync() {
        statisticService.triggerManualSync();
        return ResponseEntity.ok("Đã chạy tiến trình gom dữ liệu (Data Warehouse) thành công!");
    }

    // Lấy doanh thu theo từng tháng trong năm (vẽ biểu đồ cột cho Admin Dashboard)
    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<RevenueDTO>> getMonthlyRevenue(
            @RequestParam(required = false, defaultValue = "0") int year) {
        return ResponseEntity.ok(statisticService.getMonthlyRevenue(year));
    }
}
