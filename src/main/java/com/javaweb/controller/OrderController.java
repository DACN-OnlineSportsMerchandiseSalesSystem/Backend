package com.javaweb.controller;

//package
import com.javaweb.dto.OrderDTO;
import com.javaweb.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.javaweb.dto.OrderRequestDTO;
//library
import java.util.List;
import java.util.Date;
//springframework
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import com.javaweb.enums.OrderStatus;

@RestController
@RequestMapping("/api/orders") // Cổng API cho Frontend gọi
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints for managing client orders, placements, status updates, and invoicing")
public class OrderController {
    private final OrderService orderService;

    // HTTP GET: localhost:8080/api/orders (Chỉ lấy danh sách hóa đơn của tôi)
    @GetMapping
    @Operation(summary = "Get my orders", description = "Retrieve a list of all orders belonging to the authenticated customer.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Valid session missing")
    })
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(orderService.getMyOrders(email));
    }

    // HTTP GET: Tuyến đường Dành riêng cho ADMIN để xem toàn bộ Đơn hàng trên hệ
    // thống
    @GetMapping("/all")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all system orders", description = "Admin only. Retrieve a comprehensive list of all orders. Supports status filtering, date range query, and keyword search.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<List<OrderDTO>> getAllOrdersForAdmin(
            @Parameter(description = "Filter by order status", example = "PENDING")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Filter from date (inclusive)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @Parameter(description = "Filter to date (inclusive)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @Parameter(description = "Search keyword matches client name, email, or order ID", example = "An")
            @RequestParam(required = false) String keyword) {
        System.out.println(">>> [ADMIN API] Fetching all orders with status: " + status);
        return ResponseEntity.ok(orderService.getAllOrder(status, fromDate, toDate, keyword));
    }

    // HTTP POST: localhost:8080/api/orders
    @PostMapping
    @Operation(summary = "Create a new order", description = "Submit a checkout request and generate a new order under the client profile. Calculates total costs and applies optional vouchers.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or checkout constraints failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderRequestDTO request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderDTO createdOrder = orderService.createOrder(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID", description = "Retrieve complete details of a specific order. Users can only fetch their own orders, while admins can fetch any.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
        @ApiResponse(responseCode = "404", description = "Order not found with the given ID")
    })
    public ResponseEntity<OrderDTO> getById(
            @Parameter(description = "Unique database ID of the order", example = "1", required = true)
            @PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderDTO orderDTO = orderService.getOrderByIdForUser(id, email);
        return ResponseEntity.ok(orderDTO);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Modify the processing status of an existing order (e.g. shipping status).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found with the given ID")
    })
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @Parameter(description = "Unique database ID of the order", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody OrderRequestDTO request) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel or delete an order", description = "Permanently delete or cancel an order from the database system by its unique ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "240", description = "Order deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found with the given ID")
    })
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Unique ID of the order to delete", example = "1", required = true)
            @PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
