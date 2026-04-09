package com.javaweb.service.impl;

import com.javaweb.dto.AddressDTO;
import com.javaweb.dto.OrderDTO;
import com.javaweb.dto.OrderItemDTO;
import com.javaweb.dto.OrderRequestDTO;
import com.javaweb.entity.Address;
import com.javaweb.entity.OrderItems;
import com.javaweb.entity.Orders;
import com.javaweb.entity.ProductVariant;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.OrderRepository;
import com.javaweb.repository.ProductVariantRepository;
import com.javaweb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final ProductVariantRepository productVariantRepository;

	@Override
	public List<OrderDTO> getAllOrder() {
		List<Orders> orders = orderRepository.findAll();
		List<OrderDTO> orderDTOs = new ArrayList<>();
		for (Orders order : orders) {
			OrderDTO orderDTO = new OrderDTO();
			orderDTO.setId(order.getId());
			orderDTO.setNote(order.getNote());
			orderDTO.setCreateAt(order.getCreateAt());
			orderDTO.setTotalPrice(order.getTotalPrice());
			orderDTO.setShippingFee(order.getShippingFee());
			orderDTO.setReceiverName(order.getReceiverName());
			orderDTO.setPhone(order.getPhone());
			orderDTO.setStatus(order.getStatus());

			Set<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
					.map(this::mapItemToDTO)
					.collect(Collectors.toSet());
			orderDTO.setOrderItems(itemDTOs);
			/*
			 * for (OrderItems item : order.getOrderItems()) {
			 * OrderItemDTO dto = this.mapItemToDTO(item);
			 * itemDTOs.add(dto);
			 * }
			 */
			orderDTO.setBillingAddress(mapAddressToDTO(order.getBillingAddress()));
			orderDTOs.add(orderDTO);
		}
		return orderDTOs;
	}

	
	@Override
	public OrderDTO updateOrderStatus(Long id, String status) {
		Orders order = orderRepository.findById(id)
				.orElseThrow(() -> new ResouceNotFoundException("Order not found with id: " + id));
		order.setStatus(status);
		return mapToDTO(orderRepository.save(order));
	}

	@Override
	public OrderDTO deleteOrder(Long id) {
		Orders order = orderRepository.findById(id)
				.orElseThrow(() -> new ResouceNotFoundException("Order not found with id: " + id));
		// Trong Thương Mại Điện Tử không bao giờ "Xóa Cứng" mất biên lai, ta chỉ Xóa Mềm (CANCELED)
		order.setStatus("CANCELED"); 
		orderRepository.save(order);
		return mapToDTO(order);
	}

	@Override
	public OrderDTO getOrderById(Long id) {
		Orders order = orderRepository.findById(id)
				.orElseThrow(() -> new ResouceNotFoundException("Order not found with id: " + id));
		return mapToDTO(order);
	}

	@Override
	public OrderDTO createOrder(OrderRequestDTO request) {
		Orders order = new Orders();
		order.setNote(request.getNote());
		order.setShippingFee(request.getShippingFee());
		order.setReceiverName(request.getReceiverName());
		order.setPhone(request.getPhone());
		order.setStatus("PENDING"); // Đơn hàng mới nằm ở trạng thái Chờ Xử Lý

		BigDecimal total = BigDecimal.ZERO;

		// 1. Chuyển DTO thành Entity Address
		if (request.getBillingAddress() != null) {
			Address address = new Address();
			address.setStreet(request.getBillingAddress().getStreet());
			address.setCity(request.getBillingAddress().getCity());
			address.setState(request.getBillingAddress().getState());
			address.setIsDefault(request.getBillingAddress().getIsDefault());
			address.setReceiverName(request.getBillingAddress().getReceiverName());
			address.setPhone(request.getBillingAddress().getPhone());
			order.setBillingAddress(address);
		}

		// 2. Map từng món hàng (OrderItemDTO -> OrderItems)
		if (request.getItems() != null) {
			for (OrderItemDTO itemDto : request.getItems()) {
				OrderItems item = new OrderItems();
				item.setQuantity(itemDto.getQuantity());
				item.setUnitPrice(itemDto.getUnitPrice());
				item.setImageUrl(itemDto.getImageUrl());

				// Trích xuất ProductVariant gốc từ CSDL dựa theo Variant ID
				if (itemDto.getProductVariantId() != null) {
					ProductVariant variant = productVariantRepository.findById(itemDto.getProductVariantId())
							.orElseThrow(() -> new ResouceNotFoundException(
									"Variant not found: " + itemDto.getProductVariantId()));
					item.setProductVariants(variant);
					item.setProductId(variant.getProducts().getId());
				}

				order.add(item); // set relationships 2 chiều

				BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
				total = total.add(itemTotal);
			}
		}

		// 3. Tính tổng bill dồn kèm giá Ship
		if (order.getShippingFee() != null) {
			total = total.add(BigDecimal.valueOf(order.getShippingFee()));
		}
		order.setTotalPrice(total);

		return mapToDTO(orderRepository.save(order));
	}

	// --- CÁC HÀM TIỆN ÍCH DÙNG ĐỂ COPY OBJECT --- //

	private OrderDTO mapToDTO(Orders order) {
		OrderDTO dto = new OrderDTO();
		dto.setId(order.getId());
		dto.setNote(order.getNote());
		dto.setCreateAt(order.getCreateAt());
		dto.setTotalPrice(order.getTotalPrice());
		dto.setShippingFee(order.getShippingFee());
		dto.setReceiverName(order.getReceiverName());
		dto.setPhone(order.getPhone());
		dto.setStatus(order.getStatus());

		if (order.getBillingAddress() != null) {
			dto.setBillingAddress(mapAddressToDTO(order.getBillingAddress()));
		}

		if (order.getOrderItems() != null) {
			Set<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
					.map(this::mapItemToDTO)
					.collect(Collectors.toSet());
			dto.setOrderItems(itemDTOs);
		}

		return dto;
	}

	private AddressDTO mapAddressToDTO(Address address) {
		AddressDTO dto = new AddressDTO();
		dto.setId(address.getId());
		dto.setStreet(address.getStreet());
		dto.setCity(address.getCity());
		dto.setState(address.getState());
		dto.setIsDefault(address.getIsDefault());
		dto.setReceiverName(address.getReceiverName());
		dto.setPhone(address.getPhone());
		return dto;
	}

	private OrderItemDTO mapItemToDTO(OrderItems item) {
		OrderItemDTO dto = new OrderItemDTO();
		dto.setId(item.getId());
		dto.setImageUrl(item.getImageUrl());
		dto.setUnitPrice(item.getUnitPrice());
		dto.setQuantity(item.getQuantity());
		dto.setProductId(item.getProductId());
		if (item.getProductVariants() != null) {
			dto.setProductVariantId(item.getProductVariants().getId());
		}
		return dto;
	}
}
