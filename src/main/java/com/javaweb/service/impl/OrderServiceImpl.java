package com.javaweb.service.impl;

import com.javaweb.dto.*;
import com.javaweb.entity.Address;
import com.javaweb.entity.OrderItems;
import com.javaweb.entity.Orders;
import com.javaweb.entity.Product;
import com.javaweb.entity.ProductVariant;
import com.javaweb.entity.Voucher;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.*;
import com.javaweb.entity.User;
import com.javaweb.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import com.javaweb.entity.Category;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import com.javaweb.enums.OrderStatus;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final ProductVariantRepository productVariantRepository;
	private final UserRepository userRepository;
	private final VoucherRepository voucherRepository;
	private final EmailService emailService;

	@Override
	public List<OrderDTO> getAllOrder(OrderStatus status, Date fromDate, Date toDate, String keyword) {
		// Điều chỉnh toDate đến cuối ngày để bao gồm cả ngày hôm đó
		if (toDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(toDate);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			toDate = cal.getTime();
		}

		List<Orders> orders = orderRepository.findWithFilters(status, fromDate, toDate, keyword);
		return orders.stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public OrderDTO updateOrderStatus(Long id, OrderStatus status) {
		Orders order = orderRepository.findById(id)
				.orElseThrow(() -> new ResouceNotFoundException("Order not found with id: " + id));

		// Cộng điểm khi đơn hàng được đánh dấu là COMPLETED
		if (status == OrderStatus.COMPLETED && order.getStatus() != OrderStatus.COMPLETED) {
			User user = order.getUser();
			if (user != null) {
				Long currentPoints = user.getLevel() != null ? user.getLevel() : 0L;
				// 100.000 VNĐ = 1 điểm
				long addedPoints = order.getTotalPrice().divideToIntegralValue(BigDecimal.valueOf(100000)).longValue();
				user.setLevel(currentPoints + addedPoints);
				userRepository.save(user);
			}
		}

		order.setStatus(status);
		return mapToDTO(orderRepository.save(order));
	}

	@Override
	public OrderDTO deleteOrder(Long id) {
		Orders order = orderRepository.findById(id)
				.orElseThrow(() -> new ResouceNotFoundException("Order not found with id: " + id));
		// Trong Thương Mại Điện Tử không bao giờ "Xóa Cứng" mất biên lai, ta chỉ Xóa
		// Mềm (CANCELED)
		order.setStatus(OrderStatus.CANCELED);
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
	public List<OrderDTO> getMyOrders(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResouceNotFoundException("User not found: " + userEmail));

		List<Orders> orders = orderRepository.findByUserId(user.getId());
		return orders.stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public OrderDTO getOrderByIdForUser(Long id, String userEmail) {
		Orders order = orderRepository.findById(id)
				.orElseThrow(() -> new ResouceNotFoundException("Order not found with id: " + id));

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResouceNotFoundException("User not found"));

		boolean isAdmin = user.getRole() != null && user.getRole().getName().equalsIgnoreCase("ADMIN");
		boolean isOwner = order.getUser() != null && order.getUser().getId().equals(user.getId());

		if (!isAdmin && !isOwner) {
			throw new RuntimeException("Xin lỗi, bạn không có quyền lấy hóa đơn của người khác!");
		}

		return mapToDTO(order);
	}

	@Override
	public OrderDTO createOrder(OrderRequestDTO request, String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResouceNotFoundException("User not found with email: " + userEmail));

		Orders order = new Orders();
		order.setUser(user);
		order.setNote(request.getNote());
		order.setShippingFee(30000L); // Fix cứng phí ship là 30k để chống Cheat
		order.setReceiverName(request.getReceiverName());
		order.setPhone(request.getPhone());
		order.setStatus(OrderStatus.PENDING); // Đơn hàng mới nằm ở trạng thái Chờ Xử Lý

		BigDecimal total = BigDecimal.ZERO;

		// 1. Trích xuất thông tin giao hàng đắp thẳng vào Đơn Hàng (Snapshot)
		if (request.getBillingAddress() != null) {
			order.setBillingStreet(request.getBillingAddress().getStreet());
			order.setBillingCity(request.getBillingAddress().getCity());
			order.setBillingState(request.getBillingAddress().getState());
			if (request.getBillingAddress().getReceiverName() != null) {
				order.setReceiverName(request.getBillingAddress().getReceiverName());
			}
			if (request.getBillingAddress().getPhone() != null) {
				order.setPhone(request.getBillingAddress().getPhone());
			}
		}

		// 2. Map từng món hàng (OrderItemRequestDTO -> OrderItems)
		if (request.getItems() != null) {
			for (OrderItemRequestDTO itemDto : request.getItems()) {
				OrderItems item = new OrderItems();
				item.setQuantity(itemDto.getQuantity());

				if (itemDto.getProductVariantId() != null) {
					ProductVariant variant = productVariantRepository.findById(itemDto.getProductVariantId())
							.orElseThrow(() -> new ResouceNotFoundException(
									"Variant not found: " + itemDto.getProductVariantId()));

					// KIỂM TRA VÀ TRỪ TỒN KHO
					if (variant.getStockQuantity() < itemDto.getQuantity()) {
						throw new RuntimeException("Sản phẩm " + variant.getProducts().getName() + " (Size: "
								+ variant.getSize() + ") không đủ hàng trong kho! Cần: " + itemDto.getQuantity()
								+ ", Còn: " + variant.getStockQuantity());
					}
					variant.setStockQuantity(variant.getStockQuantity() - itemDto.getQuantity());
					productVariantRepository.save(variant);

					item.setProductVariants(variant);

					// Gán giá chốt tại thời điểm mua từ Variant
					item.setPriceAtPurchase(variant.getPrice());
					// Nếu có logic giảm giá từng món (Flash Sale), gán vào đây
					item.setDiscountAmount(BigDecimal.ZERO);
				}

				order.add(item); // set relationships 2 chiều

				BigDecimal actualPrice = item.getPriceAtPurchase().subtract(item.getDiscountAmount());
				BigDecimal itemTotal = actualPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
				total = total.add(itemTotal);
			}
		}

		// 3. Xử lý Mã Giảm Giá (Voucher)
		if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
			Voucher voucher = voucherRepository.findByCode(request.getVoucherCode())
					.orElseThrow(() -> new ResouceNotFoundException(
							"Mã giảm giá không hợp lệ: " + request.getVoucherCode()));

			// Kiểm tra hạn sử dụng
			if (voucher.getExpiryDate() != null && voucher.getExpiryDate().before(new Date())) {
				throw new RuntimeException("Mã giảm giá đã hết hạn!");
			}

			// Kiểm tra số lượt dùng
			if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
				throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
			}

			// Tính tổng tiền các sản phẩm hợp lệ để áp dụng voucher
			BigDecimal eligibleTotal = BigDecimal.ZERO;
			if (voucher.getCategory() == null && voucher.getBrand() == null) {
				// Áp dụng cho toàn bộ đơn hàng
				eligibleTotal = total;
			} else {
				for (OrderItems item : order.getOrderItems()) {
					if (item.getProductVariants() != null && item.getProductVariants().getProducts() != null) {
						Product product = item.getProductVariants().getProducts();
						boolean isEligible = false;

						if (voucher.getCategory() != null && product.getCategories() != null) {
							for (Category cat : product.getCategories()) {
								if (cat.getId().equals(voucher.getCategory().getId())) {
									isEligible = true;
									break;
								}
							}
						}
						if (voucher.getBrand() != null && product.getBrand() != null
								&& product.getBrand().getId().equals(voucher.getBrand().getId())) {
							isEligible = true;
						}

						if (isEligible) {
							BigDecimal actualPrice = item.getPriceAtPurchase().subtract(item.getDiscountAmount());
							BigDecimal itemTotal = actualPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
							eligibleTotal = eligibleTotal.add(itemTotal);
						}
					}
				}
			}

			// Kiểm tra giá trị tối thiểu của đơn hàng dựa trên các sản phẩm hợp lệ
			if (voucher.getMinOrderValue() != null && eligibleTotal.compareTo(voucher.getMinOrderValue()) < 0) {
				throw new RuntimeException(
						"Tổng tiền các sản phẩm thuộc danh mục/thương hiệu ưu đãi chưa đạt tối thiểu "
								+ voucher.getMinOrderValue() + " để áp dụng mã này!");
			}

			// Áp dụng giảm giá (tối đa bằng tổng tiền sản phẩm hợp lệ)
			BigDecimal discountToApply = voucher.getDiscountAmount();
			if (discountToApply.compareTo(eligibleTotal) > 0) {
				discountToApply = eligibleTotal;
			}

			total = total.subtract(discountToApply);
			if (total.compareTo(BigDecimal.ZERO) < 0) {
				total = BigDecimal.ZERO; // Không cho phép tổng tiền âm
			}

			// Tăng lượt dùng và gán voucher vào hóa đơn
			voucher.setUsedCount(voucher.getUsedCount() + 1);
			voucherRepository.save(voucher);
			order.setVoucher(voucher);
		}

		// 4. Tính tổng bill dồn kèm giá Ship (Mặc định 30k)
		total = total.add(BigDecimal.valueOf(30000));
		order.setTotalPrice(total);

		Orders savedOrder = orderRepository.save(order);

		// Gửi Email thông báo bất đồng bộ
		try {
			emailService.confirmOrder(userEmail, request, total);
		} catch (Exception e) {
			System.err.println("Không thể gửi email xác nhận: " + e.getMessage());
		}

		return mapToDTO(savedOrder);
	}

	// --- CÁC HÀM TIỆN ÍCH DÙNG ĐỂ COPY OBJECT --- //

	private OrderDTO mapToDTO(Orders order) {
		OrderDTO dto = new OrderDTO();
		dto.setId(order.getId());
		dto.setNote(order.getNote());
		dto.setCreateAt(order.getCreateAt());
		dto.setTotalPrice(order.getTotalPrice());
		dto.setShippingFee(order.getShippingFee());
		dto.setStatus(order.getStatus());

		// Nặn vỏ bọc AddressDTO từ Hóa đơn (Snapshot)
		if (order.getBillingStreet() != null || order.getBillingCity() != null) {
			AddressDTO fakeAddress = new AddressDTO();
			fakeAddress.setStreet(order.getBillingStreet());
			fakeAddress.setCity(order.getBillingCity());
			fakeAddress.setState(order.getBillingState());
			fakeAddress.setReceiverName(order.getReceiverName());
			fakeAddress.setPhone(order.getPhone());
			dto.setBillingAddress(fakeAddress);
		}

		if (order.getOrderItems() != null) {
			Set<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
					.map(this::mapItemToDTO)
					.collect(Collectors.toSet());
			dto.setOrderItems(itemDTOs);
		}

		if (order.getVoucher() != null) {
			com.javaweb.dto.VoucherDTO voucherDTO = new VoucherDTO();
			voucherDTO.setCode(order.getVoucher().getCode());
			voucherDTO.setDiscountAmount(order.getVoucher().getDiscountAmount());
			dto.setVoucher(voucherDTO);
		}

		return dto;
	}

	private OrderItemDTO mapItemToDTO(OrderItems item) {
		OrderItemDTO dto = new OrderItemDTO();
		dto.setId(item.getId());
		dto.setPriceAtPurchase(item.getPriceAtPurchase());
		dto.setDiscountAmount(item.getDiscountAmount());
		dto.setQuantity(item.getQuantity());
		if (item.getProductVariants() != null) {
			dto.setProductVariantId(item.getProductVariants().getId());
			dto.setSize(item.getProductVariants().getSize());
			dto.setColor(item.getProductVariants().getColor());
			if (item.getProductVariants().getProducts() != null) {
				dto.setProductName(item.getProductVariants().getProducts().getName());
				// Lấy ảnh đầu tiên của sản phẩm làm hình đại diện
				if (item.getProductVariants().getProducts().getProductImages() != null
						&& !item.getProductVariants().getProducts().getProductImages().isEmpty()) {
					dto.setImageUrl(
							item.getProductVariants().getProducts().getProductImages().iterator().next().getImageUrl());
				}
			}
		}
		return dto;
	}
}
