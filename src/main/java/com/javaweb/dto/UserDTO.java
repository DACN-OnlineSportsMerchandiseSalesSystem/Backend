package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private String roleName; // Chú ý: Chỉ trả về TÊN quyền (VD: "ADMIN"), chứ không trả nguyên object Role
}