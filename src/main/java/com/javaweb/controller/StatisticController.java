package com.javaweb.controller;

import com.javaweb.dto.*;
import com.javaweb.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics & Analytics", description = "Endpoints for retrieving business performance metrics, sales reports, revenue diagrams, and ETL triggers")
public class StatisticController {

    private final StatisticService statisticService;

    // Lấy dữ liệu kho để vẽ biểu đồ
    @GetMapping("/daily")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get daily store metrics", description = "Admin only. Retrieve day-by-day aggregated metrics (visits, sales, signups) for a given month and year.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved daily statistics list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<List<DailyStatisticDTO>> getDailyStatistics(
            @Parameter(description = "Filter by month (1-12, default/0 for all)", example = "5")
            @RequestParam(required = false, defaultValue = "0") int month,
            @Parameter(description = "Filter by calendar year (default/0 for current year)", example = "2026")
            @RequestParam(required = false, defaultValue = "0") int year) {
        
        return ResponseEntity.ok(statisticService.getDailyStatistics(month, year));
    }

    // Lấy top sản phẩm bán chạy nhất
    @GetMapping("/top-selling")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get top selling products report", description = "Admin only. Retrieve list of best performing products measured by items sold, filtered by dates.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top selling products list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProducts(
            @Parameter(description = "Filter by month (1-12)", example = "5")
            @RequestParam(required = false, defaultValue = "0") int month,
            @Parameter(description = "Filter by year", example = "2026")
            @RequestParam(required = false, defaultValue = "0") int year,
            @Parameter(description = "Maximum size of products list to return", example = "5")
            @RequestParam(required = false, defaultValue = "5") int limit) {
        
        return ResponseEntity.ok(statisticService.getTopSellingProducts(month, year, limit));
    }

    // Nút bấm ma thuật: Kích hoạt ETL gom dữ liệu ngay lập tức (Dùng lúc Demo đồ án)
    @PostMapping("/trigger-sync")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Trigger ETL manual data sync", description = "Admin only. Manually runs the data warehouse aggregation (ETL process) to sync metrics immediately instead of waiting for cron.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ETL sync triggered and executed successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<String> triggerSync() {
        statisticService.triggerManualSync();
        return ResponseEntity.ok("Đã chạy tiến trình gom dữ liệu (Data Warehouse) thành công!");
    }

    // Lấy doanh thu theo từng tháng trong năm (vẽ biểu đồ cột cho Admin Dashboard)
    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get monthly revenue stats", description = "Admin only. Get a monthly breakdown of total generated revenue for a given year to draw admin charts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly revenue breakdown"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<List<RevenueDTO>> getMonthlyRevenue(
            @Parameter(description = "Calendar year to retrieve revenue", example = "2026")
            @RequestParam(required = false, defaultValue = "0") int year) {
        return ResponseEntity.ok(statisticService.getMonthlyRevenue(year));
    }
}
