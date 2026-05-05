package com.javaweb.service;

import com.javaweb.dto.DailyStatisticDTO;

import com.javaweb.dto.TopSellingProductDTO;

import java.time.LocalDate;
import java.util.List;

public interface StatisticService {
    void syncDailyStatistic(LocalDate date);
    List<DailyStatisticDTO> getDailyStatistics(int month, int year);
    void triggerManualSync();
    List<TopSellingProductDTO> getTopSellingProducts(int month, int year, int limit);
}
