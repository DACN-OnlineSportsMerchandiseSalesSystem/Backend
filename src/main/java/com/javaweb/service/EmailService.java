package com.javaweb.service;

import com.javaweb.dto.OrderItemRequestDTO;
import com.javaweb.dto.OrderRequestDTO;

import com.javaweb.entity.ProductVariant;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private final ProductVariantRepository productVariantRepository;

    @Async
    public void confirmOrder(String toEmail, OrderRequestDTO request, BigDecimal amount) {
        StringBuilder temp = new StringBuilder(); // Tránh NullPointerException khi dùng concat

        for (OrderItemRequestDTO item : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(
                            () -> new ResouceNotFoundException("Variant not found: " + item.getProductVariantId()));

            // Dùng \n thay vì /n để xuống dòng
            temp.append("\n- ").append(variant.getProducts().getName()).append(" (SL: ").append(item.getQuantity())
                    .append(")");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Thông báo đặt hàng thành công tại Sportzone");
        message.setText("Cảm ơn bạn đã đặt hàng.\n\nChi tiết đơn hàng của bạn:" + temp.toString()
                + "\n\nTổng giá tiền: " + amount + " VND\n\nTrân trọng,\nĐội ngũ SportZone.");

        mailSender.send(message);
    }

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận Đăng ký tài khoản SportZone!");
        message.setText("Chào bạn,\n\nMã xác nhận (OTP) để đăng ký tài khoản của bạn là: " + otp
                + "\n\nMã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nĐội ngũ phát triển.");

        mailSender.send(message);
    }
}
