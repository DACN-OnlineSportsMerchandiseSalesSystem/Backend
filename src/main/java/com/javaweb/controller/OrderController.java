package com.javaweb.controller;

import com.javaweb.dto.OrderDTO;
import com.javaweb.service.OrderService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/orders") // Cổng API cho Frontend gọi
@RequiredArgsConstructor

public class OrderController {
    private final OrderService OrderService;

    // HTTP GET: localhost:8080/api/orders
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAll() {
        return ResponseEntity.ok(OrderService.getAllOrder());
    }
}
