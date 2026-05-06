package com.javaweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private StringRedisTemplate redisTemplate;
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
