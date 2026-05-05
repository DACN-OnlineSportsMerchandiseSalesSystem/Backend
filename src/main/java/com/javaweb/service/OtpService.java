package com.javaweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    private static final long OTP_VALID_DURATION = 5; // 5 phút

    // 1. Hàm tạo mã OTP 6 số ngẫu nhiên
    public String generateOtp(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Sinh số từ 100000 -> 999999
        String otpStr = String.valueOf(otp);

        // Lưu vào Redis với Key là "otp:email", tồn tại trong 5 phút
        redisTemplate.opsForValue().set("otp:" + email, otpStr, OTP_VALID_DURATION, TimeUnit.MINUTES);

        return otpStr;
    }

    // 2. Hàm gửi Email
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận Đăng ký tài khoản SportZone!");
        message.setText("Chào bạn,\n\nMã xác nhận (OTP) để đăng ký tài khoản của bạn là: " + otp + "\n\nMã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nĐội ngũ phát triển.");

        mailSender.send(message);
    }

    // 3. Hàm kiểm tra mã OTP
    public boolean validateOtp(String email, String inputOtp) {
        String key = "otp:" + email;
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp != null && savedOtp.equals(inputOtp)) {
            // Xác thực thành công -> Xóa mã khỏi Redis ngay lập tức để tránh dùng lại
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
