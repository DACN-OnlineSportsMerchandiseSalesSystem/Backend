package com.javaweb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDTO {
    private String label;       // Ví dụ: "Tháng 1", "Tháng 2", ...
    private BigDecimal revenue; // Tổng doanh thu
    private int orderCount;     // Số đơn hàng thành công
}
