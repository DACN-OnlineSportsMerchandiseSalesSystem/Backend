package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DailyStatisticDTO {
    private LocalDate statDate;
    private BigDecimal revenue;
    private Integer orderCount;
    private Integer newUserCount;
}
