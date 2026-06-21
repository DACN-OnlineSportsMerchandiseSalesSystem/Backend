package com.javaweb.service.impl;

import com.javaweb.config.MoMoConfig;
import com.javaweb.config.VNPayConfig;
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

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final MoMoConfig moMoConfig;
    private final VNPayConfig vnPayConfig;
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
        System.out.println("Received MOMO IPN: " + payload);

        String partnerCode = payload.get("partnerCode");
        String orderId = payload.get("orderId");
        String requestId = payload.get("requestId");
        String amount = String.valueOf(payload.get("amount")); 
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
            return;
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

    @Override
    public String createVNPayPayment(Long orderId, String ipAddr) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResouceNotFoundException("Order not found"));

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        long amount = order.getTotalPrice().longValue() * 100;
        String vnp_CurrCode = "VND";
        String vnp_TxnRef = orderId + "_" + System.currentTimeMillis();
        String vnp_OrderInfo = "Thanh toan don hang " + orderId;
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
        String vnp_IpAddr = ipAddr;

        java.util.Calendar cld = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Etc/GMT+7"));
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII));
                // Build query
                query.append(java.net.URLEncoder.encode(fieldName, java.nio.charset.StandardCharsets.US_ASCII));
                query.append('=');
                query.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnPayConfig.getApiUrl() + "?" + queryUrl;
    }

    @Override
    public void processVNPayIpn(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");

        // Sort and hash
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(java.net.URLEncoder.encode(fieldValue, java.nio.charset.StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        if (calculatedHash.equals(vnp_SecureHash)) {
            if ("00".equals(params.get("vnp_ResponseCode"))) {
                String txnRef = params.get("vnp_TxnRef");
                Long orderId = Long.parseLong(txnRef.split("_")[0]);
                long amount = Long.parseLong(params.get("vnp_Amount")) / 100;

                Orders order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new ResouceNotFoundException("Order not found: " + orderId));

                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                Payment payment = new Payment();
                payment.setPaymentMethod("VNPAY");
                payment.setAmount(amount);
                payment.setTransactionCode(params.get("vnp_TransactionNo"));
                payment.setOrders(order);
                paymentRepository.save(payment);
                System.out.println("VNPay IPN: Payment Successful for Order " + orderId);
            } else {
                System.err.println("VNPay IPN: Payment Failed with ResponseCode " + params.get("vnp_ResponseCode"));
            }
        } else {
            System.err.println("VNPay IPN: Invalid Signature!");
        }
    }
}
