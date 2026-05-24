package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import com.javaweb.enums.Gender;
import com.javaweb.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

@Getter
@Setter
public class UserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String roleName; // Ví dụ truyền "ADMIN" hoặc "USER"
    private UserStatus status;
    private Gender	gender;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private Date birthDate;
    private String turnstileToken; // Bổ sung cho Cloudflare Turnstile
    private String otp; // Bổ sung cho OTP Email
}
