package com.javaweb.service;

import java.util.Map;

public interface PaymentService {
    String createMoMoPayment(Long orderId);
    void processMoMoIpn(Map<String, String> payload);

    String createVNPayPayment(Long orderId, String ipAddr);
    void processVNPayIpn(Map<String, String> params);
}
