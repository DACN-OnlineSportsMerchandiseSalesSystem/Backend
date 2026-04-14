package com.javaweb.controller;

import com.javaweb.dto.JwtAuthResponse;
import com.javaweb.dto.LoginDTO;
import com.javaweb.dto.UserDTO;
import com.javaweb.dto.UserRequestDTO;
import com.javaweb.security.JwtTokenProvider;
import com.javaweb.service.UserService;
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

    // 1. TÍNH NĂNG ĐĂNG NHẬP
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDTO loginDto) {
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

    // 2. TÍNH NĂNG ĐĂNG KÍ
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserRequestDTO requestDto) {
        // Đảm bảo khách hàng tự đăng ký luôn mặc định là "USER"
        requestDto.setRoleName("USER");
        UserDTO newUser = userService.createUser(requestDto);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
}
