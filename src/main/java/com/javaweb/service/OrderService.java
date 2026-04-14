package com.javaweb.service;

import com.javaweb.dto.OrderDTO;
import java.util.List;

public interface OrderService {
	List<OrderDTO> getAllOrder();

	OrderDTO getOrderById(Long id);

	OrderDTO createOrder(com.javaweb.dto.OrderRequestDTO request, String userEmail);

	OrderDTO updateOrderStatus(Long id, String status);

	OrderDTO deleteOrder(Long id);
}
