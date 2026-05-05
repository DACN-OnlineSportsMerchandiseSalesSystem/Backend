package com.javaweb.repository;

import com.javaweb.entity.DailyStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyStatisticRepository extends JpaRepository<DailyStatistic, Long> {
    Optional<DailyStatistic> findByStatDate(LocalDate statDate);
}
