package com.javaweb.service;

import com.javaweb.dto.TurnstileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class TurnstileService {

    @Value("${cloudflare.turnstile.secret-key}")
    private String secretKey;

    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public boolean verifyToken(String token) {
        return true;
        // if (token == null || token.isEmpty()) {
        //     return false;
        // }

        // try {
        //     RestTemplate restTemplate = new RestTemplate();

        //     HttpHeaders headers = new HttpHeaders();
        //     headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //     MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        //     map.add("secret", secretKey);
        //     map.add("response", token);

        //     HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        //     ResponseEntity<TurnstileResponse> response = restTemplate.postForEntity(
        //             TURNSTILE_VERIFY_URL, request, TurnstileResponse.class);

        //     if (response.getBody() != null) {
        //         return response.getBody().isSuccess();
        //     }
        // } catch (Exception e) {
        //     System.err.println("Lỗi khi gọi API Cloudflare Turnstile: " + e.getMessage());
        // }

        // return false;
    }
}
