package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String roleName; // Ví dụ truyền "ADMIN" hoặc "USER"
    private String status;
    private String turnstileToken; // Bổ sung cho Cloudflare Turnstile
    private String otp; // Bổ sung cho OTP Email
}
