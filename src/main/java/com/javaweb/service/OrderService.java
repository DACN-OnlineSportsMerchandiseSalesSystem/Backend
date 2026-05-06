package com.javaweb.service;

import com.javaweb.dto.OrderDTO;
import java.util.List;

public interface OrderService {
	List<OrderDTO> getAllOrder(String status, java.util.Date fromDate, java.util.Date toDate, String keyword);

	OrderDTO getOrderById(Long id);
	
	// Lấy danh sách hóa đơn cá nhân
	List<OrderDTO> getMyOrders(String userEmail);
	
	// Lấy hóa đơn chi tiết có check quyền sỡ hữu
	OrderDTO getOrderByIdForUser(Long id, String userEmail);
	OrderDTO createOrder(com.javaweb.dto.OrderRequestDTO request, String userEmail);

	OrderDTO updateOrderStatus(Long id, String status);

	OrderDTO deleteOrder(Long id);
}
