package com.javaweb.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class OrderDTO {
    private Long id;
    private Date createAt;
    private String note; 	
    private BigDecimal totalPrice;  
    private Long shippingFee;
    private String receiverName;
    private String phone;
    private String status;
    private Set<OrderItemDTO> orderItems = new HashSet<>();
    private AddressDTO billingAddress;
}
