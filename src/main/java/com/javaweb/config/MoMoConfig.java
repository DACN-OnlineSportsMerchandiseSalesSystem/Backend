package com.javaweb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@ConfigurationProperties(prefix = "momo")
@Getter
@Setter
public class MoMoConfig {
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String apiUrl;
    private String returnUrl;
    private String ipnUrl;

    public String hashHmacSHA256(String data) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] hashBytes = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexStringBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexStringBuilder.append(hex);
            }
            return hexStringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }
}
