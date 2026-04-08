package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String roleName; // Ví dụ truyền "ADMIN" hoặc "USER"
    private String status;
}
