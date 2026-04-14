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

    // HTTP GET: localhost:8080/api/orders
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAll() {
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
        // Dùng Exception toàn cục, không cần check null nữa
        OrderDTO orderDTO = orderService.getOrderById(id);
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
