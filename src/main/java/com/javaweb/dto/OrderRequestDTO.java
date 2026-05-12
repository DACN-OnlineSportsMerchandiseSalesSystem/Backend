package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import com.javaweb.enums.OrderStatus;

@Getter
@Setter
public class OrderRequestDTO {
    private String note;
    private String receiverName;
    private String phone;
    private OrderStatus status;

    // Cấu trúc địa chỉ giao hàng và danh sách món hàng khách mua
    private AddressDTO billingAddress;
    private List<OrderItemRequestDTO> items;
    
    // Thêm mã giảm giá
    private String voucherCode;
}
