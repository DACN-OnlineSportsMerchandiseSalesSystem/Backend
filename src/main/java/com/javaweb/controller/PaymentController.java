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
@Tag(name = "Payment Management", description = "Endpoints for managing MoMo e-wallet payment integrations, creating pay URLs, and receiving IPN webhooks")
public class PaymentController {

    private final PaymentService paymentService;

    // API 1: Frontend gọi để lấy link thanh toán (Ví dụ lúc bấm nút "Thanh Toán MOMO")
    @PostMapping("/momo/create")
    @Operation(summary = "Create MoMo payment URL", description = "Submit a checkout order ID to generate an official MoMo checkout payment redirect URL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated MoMo checkout redirect URL"),
        @ApiResponse(responseCode = "400", description = "Invalid order ID or MoMo provider connection issue")
    })
    public ResponseEntity<String> createMoMoPayment(
            @Parameter(description = "Unique checkout order ID", example = "1", required = true)
            @RequestParam Long orderId) {
        String payUrl = paymentService.createMoMoPayment(orderId);
        return ResponseEntity.ok(payUrl);
    }

    // API 2: Webhook (IPN) - MOMO sẽ tự động gọi API này khi khách thanh toán xong
    // Lưu ý: MOMO bắn POST request dạng JSON
    @PostMapping("/momo/ipn")
    @Operation(summary = "MoMo Webhook Instant Payment Notification (IPN)", description = "Public webhook endpoint consumed exclusively by MoMo servers to notify the platform of transaction outcomes asynchronously.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "240", description = "MoMo notification processed successfully")
    })
    public ResponseEntity<Void> handleMoMoIpn(@RequestBody Map<String, Object> payload) {
        // Convert Map<String, Object> to Map<String, String> simply
        Map<String, String> stringPayload = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            stringPayload.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        try {
            paymentService.processMoMoIpn(stringPayload);
        } catch (Exception e) {
            System.err.println("Error processing MOMO IPN: " + e.getMessage());
        }
        
        // Luôn trả về 204 No Content cho MOMO biết là đã nhận được (tránh MOMO gọi lại liên tục)
        return ResponseEntity.noContent().build();
    }
}
