package com.javaweb.exception; // Lưu tạm ErrorDetails vào đây giống cách bạn khai báo

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorDetails {
    private String message;
    private String details;
}
