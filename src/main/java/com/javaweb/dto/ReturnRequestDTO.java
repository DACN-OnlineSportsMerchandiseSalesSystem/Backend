package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ReturnRequestDTO {
    private Long id;
    private Long orderId;
    private Long userId;
    private String status;
    private BigDecimal refundAmount;
    private String reason;
    private Date createdAt;
    private List<ReturnItemDTO> returnItems;
}
