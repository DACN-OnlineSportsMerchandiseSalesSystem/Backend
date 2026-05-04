package com.javaweb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class TtsService {

    @Value("${fpt.api.key}")
    private String apiKey;

    @Value("${fpt.tts.voice}")
    private String voice;

    @Value("${fpt.tts.speed}")
    private String speed;

    private final String FPT_TTS_URL = "https://api.fpt.ai/hmi/tts/v5";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getTextToSpeechUrl(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.set("voice", voice);
            headers.set("speed", speed);
            // Quan trọng: Chỉ định rõ UTF-8 để không bị lỗi font tiếng Việt
            headers.setContentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8)); 

            HttpEntity<String> entity = new HttpEntity<>(text, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(FPT_TTS_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Kiểm tra mã lỗi từ FPT.AI (0 là thành công)
                Object errorCode = response.getBody().get("error");
                if (errorCode != null && !errorCode.toString().equals("0")) {
                    String errorMsg = (String) response.getBody().get("message");
                    System.err.println("!!! FPT.AI Error (Code " + errorCode + "): " + errorMsg);
                    System.err.println("!!! Văn bản lỗi: " + text);
                    return null;
                }

                String asyncUrl = (String) response.getBody().get("async");
                if (asyncUrl != null && !asyncUrl.isEmpty()) {
                    System.out.println(">>> Link âm thanh từ FPT.AI: " + asyncUrl);
                    return asyncUrl;
                }
            } else {
                System.err.println("!!! FPT.AI trả về lỗi HTTP: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("!!! Lỗi gọi FPT.AI TTS: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
