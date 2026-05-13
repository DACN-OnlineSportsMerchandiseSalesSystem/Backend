package com.javaweb.service.impl;

import com.javaweb.dto.*;
import com.javaweb.entity.DailyStatistic;
import com.javaweb.repository.*;
import com.javaweb.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final DailyStatisticRepository statisticRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Tự động chạy vào lúc 23:59:59 mỗi đêm.
     * Cú pháp Cron: Giây Phút Giờ Ngày Tháng Thứ
     */
    @Scheduled(cron = "59 59 23 * * ?")
    public void nightlySync() {
        System.out.println("Bắt đầu tiến trình ETL (Data Warehouse) cho ngày hôm nay...");
        syncDailyStatistic(LocalDate.now());
    }

    @Override
    public void triggerManualSync() {
        System.out.println("Admin kích hoạt đồng bộ dữ liệu thủ công!");
        // Chạy đồng bộ cho ngày hôm nay ngay lập tức (Phục vụ cho lúc Demo)
        syncDailyStatistic(LocalDate.now());
    }

    @Override
    public void syncDailyStatistic(LocalDate date) {
        // 1. Xác định khung giờ: Từ 00:00:00 đến 23:59:59 của ngày cần tính
        Date startOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 2. Extract & Transform (Kéo dữ liệu từ các bảng và Tính toán)
        BigDecimal dailyRevenue = orderRepository.sumRevenueByDateRange(startOfDay, endOfDay);
        if (dailyRevenue == null)
            dailyRevenue = BigDecimal.ZERO;

        Integer orderCount = orderRepository.countOrdersByDateRange(startOfDay, endOfDay);
        if (orderCount == null)
            orderCount = 0;

        Integer newUserCount = userRepository.countNewUsersByDateRange(startOfDay, endOfDay);
        if (newUserCount == null)
            newUserCount = 0;

        // 3. Load (Lưu vào kho dữ liệu DailyStatistic)
        DailyStatistic statistic = statisticRepository.findByStatDate(date).orElse(new DailyStatistic());
        statistic.setStatDate(date);
        statistic.setRevenue(dailyRevenue);
        statistic.setOrderCount(orderCount);
        statistic.setNewUserCount(newUserCount);

        statisticRepository.save(statistic);
        System.out.println("Đã đồng bộ thành công dữ liệu ngày " + date + " vào Data Warehouse.");
    }

    @Override
    public List<DailyStatisticDTO> getDailyStatistics(int month, int year) {
        // Ở thực tế sẽ cần lấy theo tháng năm, tạm thời lấy tất cả để demo vẽ biểu đồ
        return statisticRepository.findAll().stream().map(stat -> {
            DailyStatisticDTO dto = new DailyStatisticDTO();
            dto.setStatDate(stat.getStatDate());
            dto.setRevenue(stat.getRevenue());
            dto.setOrderCount(stat.getOrderCount());
            dto.setNewUserCount(stat.getNewUserCount());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TopSellingProductDTO> getTopSellingProducts(int month, int year, int limit) {
        LocalDate startDate;
        LocalDate endDate;

        if (month > 0 && year > 0) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.plusMonths(1);
        } else {
            // Mặc định lấy từ đầu năm đến hiện tại nếu không truyền đủ tháng năm
            startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
            endDate = LocalDate.of( LocalDate.now().getYear(),
                                    LocalDate.now().getMonth().getValue(),
                                    LocalDate.now().getDayOfMonth());
        }

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Pageable topTen = PageRequest.of(0, limit);
        return productRepository.getTopSellingProducts(start, end, topTen);
    }

    @Override
    public List<RevenueDTO> getMonthlyRevenue(int year) {
        int targetYear = (year > 0) ? year : LocalDate.now().getYear();

        List<Object[]> raw = orderRepository.getMonthlyRevenue(targetYear);

        // Tạo mảng 12 tháng mặc định = 0 (các tháng chưa có đơn hàng)
        BigDecimal[] revenues = new BigDecimal[12];
        int[] counts = new int[12];
        for (int i = 0; i < 12; i++) {
            revenues[i] = BigDecimal.ZERO;
            counts[i] = 0;
        }

        // Đổ dữ liệu từ DB vào mảng
        for (Object[] row : raw) {
            int month = ((Number) row[0]).intValue(); // MONTH() trả về 1-12
            BigDecimal revenue = (BigDecimal) row[1];
            long count = ((Number) row[2]).longValue();
            revenues[month - 1] = revenue != null ? revenue : BigDecimal.ZERO;
            counts[month - 1] = (int) count;
        }

        // Build kết quả đủ 12 tháng
        List<RevenueDTO> result = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            result.add(new RevenueDTO("Tháng " + (i + 1), revenues[i], counts[i]));
        }
        return result;
    }
}
