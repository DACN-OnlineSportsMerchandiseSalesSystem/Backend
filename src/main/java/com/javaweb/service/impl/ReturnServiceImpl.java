package com.javaweb.service.impl;

import com.javaweb.dto.CreateReturnRequestDTO;
import com.javaweb.dto.ReturnItemDTO;
import com.javaweb.dto.ReturnItemRequestDTO;
import com.javaweb.dto.ReturnRequestDTO;
import com.javaweb.entity.*;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.OrderRepository;
import com.javaweb.repository.ReturnRequestRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public ReturnRequestDTO createReturnRequest(Long userId, CreateReturnRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResouceNotFoundException("User not found"));

        Orders order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new ResouceNotFoundException("Order not found"));

        // Kiểm tra đơn hàng có thuộc về user này không
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên đơn hàng này.");
        }

        // Chỉ cho phép trả hàng khi đơn hàng ở trạng thái PAID hoặc DELIVERED
        if (!"PAID".equals(order.getStatus()) && !"DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể yêu cầu trả hàng cho đơn hàng đã Thanh Toán hoặc Đã Giao.");
        }

        // Kiểm tra xem đơn hàng này đã có yêu cầu trả hàng PENDING chưa
        List<ReturnRequest> existingRequests = returnRequestRepository.findByOrderId(order.getId());
        for (ReturnRequest existing : existingRequests) {
            if ("PENDING".equals(existing.getStatus())) {
                throw new RuntimeException("Đơn hàng này đang có yêu cầu đổi trả chờ xử lý.");
            }
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setUser(user);
        returnRequest.setOrder(order);
        returnRequest.setReason(requestDTO.getReason());
        returnRequest.setStatus("PENDING");

        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        for (ReturnItemRequestDTO itemDTO : requestDTO.getItems()) {
            // Tìm OrderItem tương ứng trong Đơn hàng
            OrderItems orderItem = order.getOrderItems().stream()
                    .filter(oi -> oi.getId().equals(itemDTO.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không thuộc đơn hàng này."));

            if (itemDTO.getQuantity() > orderItem.getQuantity()) {
                throw new RuntimeException("Số lượng trả không được lớn hơn số lượng đã mua.");
            }

            ReturnItem returnItem = new ReturnItem();
            returnItem.setOrderItem(orderItem);
            returnItem.setQuantity(itemDTO.getQuantity());
            returnItem.setImageProof(itemDTO.getImageProof());

            returnRequest.addReturnItem(returnItem);

            // Tính tiền hoàn lại dựa trên giá lúc mua và số lượng muốn trả
            BigDecimal refundForItem = orderItem.getPriceAtPurchase()
                    .subtract(orderItem.getDiscountAmount() != null ? orderItem.getDiscountAmount() : BigDecimal.ZERO)
                    .multiply(new BigDecimal(itemDTO.getQuantity()));
            totalRefundAmount = totalRefundAmount.add(refundForItem);
        }

        returnRequest.setRefundAmount(totalRefundAmount);

        ReturnRequest savedRequest = returnRequestRepository.save(returnRequest);
        return mapToDTO(savedRequest);
    }

    @Override
    public List<ReturnRequestDTO> getReturnRequestsByUser(Long userId) {
        return returnRequestRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnRequestDTO> getAllReturnRequests() {
        return returnRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReturnRequestDTO processReturnRequest(Long requestId, String action) {
        ReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResouceNotFoundException("Return Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Yêu cầu này đã được xử lý trước đó.");
        }

        if ("APPROVE".equalsIgnoreCase(action)) {
            request.setStatus("APPROVED");
            // Cập nhật trạng thái Đơn hàng gốc thành REFUNDED
            Orders order = request.getOrder();
            order.setStatus("REFUNDED");
            orderRepository.save(order);
        } else if ("REJECT".equalsIgnoreCase(action)) {
            request.setStatus("REJECTED");
        } else {
            throw new RuntimeException("Hành động không hợp lệ. Sử dụng 'APPROVE' hoặc 'REJECT'.");
        }

        ReturnRequest updatedRequest = returnRequestRepository.save(request);
        return mapToDTO(updatedRequest);
    }

    private ReturnRequestDTO mapToDTO(ReturnRequest request) {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setId(request.getId());
        dto.setOrderId(request.getOrder().getId());
        dto.setUserId(request.getUser().getId());
        dto.setStatus(request.getStatus());
        dto.setRefundAmount(request.getRefundAmount());
        dto.setReason(request.getReason());
        dto.setCreatedAt(request.getCreatedAt());

        if (request.getReturnItems() != null) {
            List<ReturnItemDTO> itemDTOs = request.getReturnItems().stream().map(item -> {
                ReturnItemDTO itemDTO = new ReturnItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setOrderItemId(item.getOrderItem().getId());
                itemDTO.setProductName(item.getOrderItem().getProductVariants().getProducts().getName());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setImageProof(item.getImageProof());
                return itemDTO;
            }).collect(Collectors.toList());
            dto.setReturnItems(itemDTOs);
        }
        return dto;
    }
}
