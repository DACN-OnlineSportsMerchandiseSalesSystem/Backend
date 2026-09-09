package com.javaweb.controller;

import com.javaweb.enums.OrderStatus;
import com.javaweb.service.OrderService;
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
    private final OrderService orderService;

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
        String ipAddr = request.getHeader("X-FORWARDED-FOR");
        if (ipAddr == null || ipAddr.isEmpty() || "unknown".equalsIgnoreCase(ipAddr)) {
            ipAddr = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ipAddr)) {
            ipAddr = "127.0.0.1";
        }
        String payUrl = paymentService.createVNPayPayment(orderId, ipAddr);
        return ResponseEntity.ok(payUrl);
    }

    @GetMapping("/vnpay/return")
    @Operation(summary = "VNPay Return Callback", description = "Callback endpoint when user completes payment and is redirected back.")
    public ResponseEntity<Map<String, Object>> handleVNPayReturn(@RequestParam Map<String, String> params) {
        Map<String, Object> response = new java.util.HashMap<>();
        String responseCode = params.get("vnp_ResponseCode");
        boolean isSuccess = "00".equals(responseCode);

        if (isSuccess) {
            try {
                paymentService.processVNPayIpn(params);
            } catch (Exception e) {
                System.err.println("handleVNPayReturn IPN processing note: " + e.getMessage());
            }
        } else if ("24".equals(responseCode)) {
            // Khách hàng chủ động hủy thanh toán trên cổng VNPay
            try {
                String txnRef = params.get("vnp_TxnRef");
                if (txnRef != null) {
                    Long orderId = Long.parseLong(txnRef.split("_")[0]);
                    orderService.updateOrderStatus(orderId, OrderStatus.CANCELED);
                    System.out.println("VNPay Return: Đã hủy đơn hàng #" + orderId + " và hoàn trả tồn kho vì khách bấm Hủy");
                }
            } catch (Exception e) {
                System.err.println("handleVNPayReturn cancel order error: " + e.getMessage());
            }
        }

        response.put("success", isSuccess);
        response.put("responseCode", responseCode);
        response.put("message", isSuccess ? "Giao dịch thành công" : "Giao dịch không thành công");
        response.put("orderId", params.get("vnp_TxnRef"));
        response.put("amount", params.get("vnp_Amount") != null ? Long.parseLong(params.get("vnp_Amount")) / 100 : 0);
        response.put("transactionNo", params.get("vnp_TransactionNo"));
        response.put("bankCode", params.get("vnp_BankCode"));
        response.put("payDate", params.get("vnp_PayDate"));
        return ResponseEntity.ok(response);
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
