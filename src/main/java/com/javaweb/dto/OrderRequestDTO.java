package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDTO {
    private String note;
    private Long shippingFee;
    private String receiverName;
    private String phone;
    private String status;

    // Cấu trúc địa chỉ giao hàng và danh sách món hàng khách mua
    private AddressDTO billingAddress;
    private List<OrderItemDTO> items;
}
