package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.javaweb.enums.CartStatus;

@Getter
@Setter
public class CartDTO {
    private Long id;
    private String name;
    private CartStatus status;
    private Boolean isDefault;
    private Date createdAt;
    private Date updatedAt;
    private List<CartItemDTO> items;
    private Integer itemCount;
    private Integer totalQuantity;
    private BigDecimal totalPrice;
}
