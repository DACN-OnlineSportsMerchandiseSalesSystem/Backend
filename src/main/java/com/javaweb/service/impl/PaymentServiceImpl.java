package com.javaweb.service.impl;

import com.javaweb.config.MoMoConfig;
import com.javaweb.entity.Orders;
import com.javaweb.entity.Payment;
import com.javaweb.enums.OrderStatus;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.OrderRepository;
import com.javaweb.repository.PaymentRepository;
import com.javaweb.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final MoMoConfig moMoConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public String createMoMoPayment(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResouceNotFoundException("Order not found"));

        String orderIdStr = orderId + "_" + System.currentTimeMillis();
        String requestId = String.valueOf(System.currentTimeMillis());
        long amount = order.getTotalPrice().longValue();

        String requestType = "captureWallet";
        String extraData = "";
        String orderInfo = "Thanh toan don hang " + orderId;

        String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + moMoConfig.getIpnUrl() +
                "&orderId=" + orderIdStr +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + moMoConfig.getPartnerCode() +
                "&redirectUrl=" + moMoConfig.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = moMoConfig.hashHmacSHA256(rawSignature);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", moMoConfig.getPartnerCode());
        requestBody.put("partnerName", "Test");
        requestBody.put("storeId", "MomoTestStore");
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderIdStr);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", moMoConfig.getReturnUrl());
        requestBody.put("ipnUrl", moMoConfig.getIpnUrl());
        requestBody.put("lang", "vi");
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                moMoConfig.getApiUrl(),
                org.springframework.http.HttpMethod.POST,
                entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("payUrl")) {
            return (String) responseBody.get("payUrl");
        }
        throw new RuntimeException("Failed to generate MoMo payUrl: " + responseBody);
    }

    @Override
    public void processMoMoIpn(Map<String, String> payload) {
        System.out.println("Received IPN: " + payload);

        String partnerCode = payload.get("partnerCode");
        String orderId = payload.get("orderId");
        String requestId = payload.get("requestId");
        String amount = String.valueOf(payload.get("amount")); // Có thể MOMO trả về số
        String orderInfo = payload.get("orderInfo");
        String orderType = payload.get("orderType");
        String transId = String.valueOf(payload.get("transId"));
        String resultCode = String.valueOf(payload.get("resultCode"));
        String message = payload.get("message");
        String payType = payload.get("payType");
        String responseTime = String.valueOf(payload.get("responseTime"));
        String extraData = payload.get("extraData");
        String momoSignature = payload.get("signature");

        String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&message=" + message +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&orderType=" + orderType +
                "&partnerCode=" + partnerCode +
                "&payType=" + payType +
                "&requestId=" + requestId +
                "&responseTime=" + responseTime +
                "&resultCode=" + resultCode +
                "&transId=" + transId;

        String calculatedSignature = moMoConfig.hashHmacSHA256(rawSignature);

        if (!calculatedSignature.equals(momoSignature)) {
            System.err.println("MOMO IPN: Invalid Signature!");
            return; // Ignore if invalid
        }

        if ("0".equals(resultCode)) {
            String originalOrderIdStr = orderId.split("_")[0];
            Long originalOrderId = Long.parseLong(originalOrderIdStr);

            Orders order = orderRepository.findById(originalOrderId)
                    .orElseThrow(() -> new ResouceNotFoundException("Order not found: " + originalOrderId));

            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            Payment payment = new Payment();
            payment.setPaymentMethod("MOMO");
            payment.setAmount(Long.parseLong(amount));
            payment.setTransactionCode(transId);
            payment.setOrders(order);
            paymentRepository.save(payment);
        }
    }
}
