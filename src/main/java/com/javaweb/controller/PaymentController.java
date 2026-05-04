package com.javaweb.controller;

import com.javaweb.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // API 1: Frontend gọi để lấy link thanh toán (Ví dụ lúc bấm nút "Thanh Toán MOMO")
    @PostMapping("/momo/create")
    public ResponseEntity<String> createMoMoPayment(@RequestParam Long orderId) {
        String payUrl = paymentService.createMoMoPayment(orderId);
        return ResponseEntity.ok(payUrl);
    }

    // API 2: Webhook (IPN) - MOMO sẽ tự động gọi API này khi khách thanh toán xong
    // Lưu ý: MOMO bắn POST request dạng JSON
    @PostMapping("/momo/ipn")
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
