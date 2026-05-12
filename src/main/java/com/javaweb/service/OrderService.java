package com.javaweb.service;

import com.javaweb.dto.OrderDTO;
import java.util.List;
import java.util.Date;
import com.javaweb.dto.OrderRequestDTO;
import com.javaweb.enums.OrderStatus;

public interface OrderService {
	List<OrderDTO> getAllOrder(OrderStatus status, Date fromDate, Date toDate, String keyword);

	OrderDTO getOrderById(Long id);

	// Lấy danh sách hóa đơn cá nhân
	List<OrderDTO> getMyOrders(String userEmail);

	// Lấy hóa đơn chi tiết có check quyền sỡ hữu
	OrderDTO getOrderByIdForUser(Long id, String userEmail);

	OrderDTO createOrder(OrderRequestDTO request, String userEmail);

	OrderDTO updateOrderStatus(Long id, OrderStatus status);

	OrderDTO deleteOrder(Long id);
}
