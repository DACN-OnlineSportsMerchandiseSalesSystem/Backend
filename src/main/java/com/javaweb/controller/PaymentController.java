package com.javaweb.controller;

import com.javaweb.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for managing MoMo and VNPay payment integrations")
public class PaymentController {

    private final PaymentService paymentService;

    // ===============================
    // MOMO ENDPOINTS
    // ===============================

    @PostMapping("/momo/create")
    @Operation(summary = "Create MoMo payment URL")
    public ResponseEntity<String> createMoMoPayment(@RequestParam Long orderId) {
        String payUrl = paymentService.createMoMoPayment(orderId);
        return ResponseEntity.ok(payUrl);
    }

    @PostMapping("/momo/ipn")
    @Operation(summary = "MoMo Webhook IPN")
    public ResponseEntity<Void> handleMoMoIpn(@RequestBody Map<String, Object> payload) {
        Map<String, String> stringPayload = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            stringPayload.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        try {
            paymentService.processMoMoIpn(stringPayload);
        } catch (Exception e) {
            System.err.println("Error processing MOMO IPN: " + e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    // ===============================
    // VNPAY ENDPOINTS
    // ===============================

    @PostMapping("/vnpay/create")
    @Operation(summary = "Create VNPay payment URL", description = "Generates a VNPay payment link including QR code option.")
    public ResponseEntity<String> createVNPayPayment(
            @RequestParam Long orderId,
            jakarta.servlet.http.HttpServletRequest request) {
        String ipAddr = request.getRemoteAddr();
        String payUrl = paymentService.createVNPayPayment(orderId, ipAddr);
        return ResponseEntity.ok(payUrl);
    }

    @GetMapping("/vnpay/ipn")
    @Operation(summary = "VNPay Webhook IPN", description = "Asynchronous notification from VNPay to update order status.")
    public ResponseEntity<Map<String, String>> handleVNPayIpn(@RequestParam Map<String, String> params) {
        try {
            paymentService.processVNPayIpn(params);
            // VNPay requires a specific response format for IPN
            Map<String, String> response = new java.util.HashMap<>();
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new java.util.HashMap<>();
            response.put("RspCode", "99");
            response.put("Message", "Unknow error");
            return ResponseEntity.ok(response);
        }
    }
}
