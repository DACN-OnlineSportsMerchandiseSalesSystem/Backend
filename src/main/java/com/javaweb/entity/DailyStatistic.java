package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_statistic")
@Getter
@Setter
public class DailyStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", unique = true, nullable = false)
    private LocalDate statDate;

    @Column(name = "revenue", precision = 15, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Column(name = "new_user_count")
    private Integer newUserCount = 0;
}
