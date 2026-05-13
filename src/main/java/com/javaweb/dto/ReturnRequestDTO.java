package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.javaweb.enums.ReturnStatus;

@Getter
@Setter
public class ReturnRequestDTO {
    private Long id;
    private Long orderId;
    private Long userId;
    private ReturnStatus status;
    private BigDecimal refundAmount;
    private String reason;
    private Date createdAt;
    private List<ReturnItemDTO> returnItems;
}
