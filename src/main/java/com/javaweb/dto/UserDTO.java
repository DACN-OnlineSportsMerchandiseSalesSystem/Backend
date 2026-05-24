package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;
import com.javaweb.enums.UserStatus;
import com.javaweb.enums.RankType;
import com.javaweb.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserStatus status;
    private Gender gender;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private Date birthDate;
    private Long level;
    private RankType rank;
    private String roleName; // Chú ý: Chỉ trả về TÊN quyền (VD: "ADMIN"), chứ không trả nguyên object Role
}