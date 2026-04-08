package com.javaweb.service.impl;

import com.javaweb.dto.OrderDTO;
import com.javaweb.entity.Orders;
import com.javaweb.repository.OrderRepository;
import com.javaweb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {
	private final OrderRepository OrderRepository;

	@Override
	public List<OrderDTO> getAllOrder() {
		return null;
	}

}
