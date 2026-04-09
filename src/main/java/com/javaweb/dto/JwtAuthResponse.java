package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    // Constructor để gán token dễ dàng
    public JwtAuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
