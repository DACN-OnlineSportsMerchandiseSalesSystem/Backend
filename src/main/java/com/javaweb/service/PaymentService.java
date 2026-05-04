package com.javaweb.service;

import java.util.Map;

public interface PaymentService {
    String createMoMoPayment(Long orderId);
    void processMoMoIpn(Map<String, String> payload);
}
