package com.javaweb.controller;

//package
import com.javaweb.dto.OrderDTO;
import com.javaweb.service.OrderService;
import lombok.RequiredArgsConstructor;
import com.javaweb.dto.OrderRequestDTO;
//library
import java.util.List;
//springframework
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/orders") // Cổng API cho Frontend gọi
@RequiredArgsConstructor

public class OrderController {
    private final OrderService orderService;

    // HTTP GET: localhost:8080/api/orders (Chỉ lấy danh sách hóa đơn của tôi)
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(orderService.getMyOrders(email));
    }

    // HTTP GET: Tuyến đường Dành riêng cho ADMIN để xem toàn bộ Đơn hàng trên hệ thống
    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrdersForAdmin() {
        return ResponseEntity.ok(orderService.getAllOrder());
    }

    // HTTP POST: localhost:8080/api/orders
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderRequestDTO request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderDTO createdOrder = orderService.createOrder(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderDTO orderDTO = orderService.getOrderByIdForUser(id, email);
        return ResponseEntity.ok(orderDTO);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestBody OrderRequestDTO request) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

}
