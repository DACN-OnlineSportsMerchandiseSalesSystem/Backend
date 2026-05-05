package com.javaweb.controller;

import com.javaweb.dto.*;
import com.javaweb.security.*;
import com.javaweb.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Tất cả API bắt đầu bằng /api/auth sẽ không yêu cầu Token!
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final TurnstileService turnstileService;
    private final OtpService otpService;

    // 1. TÍNH NĂNG ĐĂNG NHẬP
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) {
        // Kiểm tra Cloudflare Turnstile Token
        if (!turnstileService.verifyToken(loginDto.getTurnstileToken())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Xác thực Bot (Turnstile) thất bại!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        // Đánh dấu người này đã đăng nhập vào phiên hệ thống
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthResponse(token));
    }

    // 2. YÊU CẦU MÃ OTP ĐỂ ĐĂNG KÝ
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody com.javaweb.dto.SendOtpRequestDTO request) {
        String email = request.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng cung cấp email!");
        }
        
        // Kiểm tra xem email đã tồn tại chưa (Chặn ngay từ lúc xin OTP)
        if (userService.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email này đã được sử dụng!");
        }

        // Tạo và gửi OTP
        String otp = otpService.generateOtp(email);
        try {
            otpService.sendOtpEmail(email, otp);
            return ResponseEntity.ok("Mã OTP đã được gửi đến email của bạn.");
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống khi gửi email OTP!");
        }
    }

    // 3. TÍNH NĂNG ĐĂNG KÍ
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDTO requestDto) {
        // 1. Kiểm tra Cloudflare Turnstile Token
        if (!turnstileService.verifyToken(requestDto.getTurnstileToken())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Xác thực Bot (Turnstile) thất bại!");
        }

        // 2. Kiểm tra trước các thông tin cơ bản (Số điện thoại, Mật khẩu, Email)
        // Để tránh việc OTP hợp lệ bị xóa mất nhưng sau đó lại lỗi trùng SĐT
        if (userService.existsByEmail(requestDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email này đã được sử dụng!");
        }
        if (requestDto.getPhone() != null && userService.existsByPhone(requestDto.getPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Số điện thoại này đã được sử dụng!");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu phải có ít nhất 6 ký tự!");
        }

        // 3. Kiểm tra mã OTP (Đây là chốt chặn cuối cùng)
        if (requestDto.getOtp() == null || requestDto.getOtp().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập mã OTP!");
        }

        if (!otpService.validateOtp(requestDto.getEmail(), requestDto.getOtp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        // 4. Nếu qua hết mọi chốt chặn -> Tạo tài khoản
        requestDto.setRoleName("CUSTOMER");
        UserDTO newUser = userService.createUser(requestDto);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
}
