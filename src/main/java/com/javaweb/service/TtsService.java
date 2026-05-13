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
            // LÀM SẠCH VĂN BẢN TRƯỚC KHI GỬI (Loại bỏ Emoji và ký tự đặc biệt)
            String cleanedText = cleanText(text);
            if (cleanedText.isEmpty()) return null;

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.set("voice", voice);
            headers.set("speed", speed);
            headers.setContentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8)); 

            System.out.println("\n>>> [FPT.AI] Đang gửi đoạn văn bản: [" + cleanedText + "]");

            // Đảm bảo Body được ép kiểu byte UTF-8
            HttpEntity<byte[]> entity = new HttpEntity<>(cleanedText.getBytes(java.nio.charset.StandardCharsets.UTF_8), headers);

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
                    // Trả về ngay lập tức để Frontend tự dùng hàm waitAudioReady chạy ngầm (Pipeline)
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

    /**
     * Tối ưu hóa văn bản trước khi gửi lên FPT.AI
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        // 1. Loại bỏ thẻ định dạng (Nếu còn sót lại từ UI)
        String cleaned = text.replaceAll("\\[TEXT\\]:|\\[VOICE\\]:", "");
        
        // 2. Xử lý từ khóa đặc biệt (Để AI đọc chuẩn tiếng Việt nhưng UI vẫn hiển thị tiếng Anh)
        cleaned = cleaned.replaceAll("(?i)SportZone", "sờ pót dôn");
        cleaned = cleaned.replaceAll("(?i)SportBot", "sờ pót bót");
        
        // 3. Thay thế TOÀN BỘ dấu gạch ngang (thường gặp trong phiên âm e-a, nai-ki) thành khoảng trắng
        cleaned = cleaned.replaceAll("-", " ");
        
        // 4. Xử lý thông minh các đơn vị đo lường/tiền tệ bị dính liền vào số (Ví dụ: 2.990.000đ -> 2.990.000 đồng, 260g -> 260 gam)
        // Dùng \d kết hợp bắt nhóm để tránh lỗi Word Boundary (\b) không hoạt động với tiếng Việt
        cleaned = cleaned.replaceAll("(\\d)\\s*(đ|₫|VND|vnd)", "$1 đồng");
        cleaned = cleaned.replaceAll("(\\d)\\s*(k|K)(?=[\\s.,]|$)", "$1 nghìn"); // 100k -> 100 nghìn
        cleaned = cleaned.replaceAll("(\\d)\\s*(kg|Kg|KG)(?=[\\s.,]|$)", "$1 kí lô gam");
        cleaned = cleaned.replaceAll("(\\d)\\s*(g|G)(?=[\\s.,]|$)", "$1 gam");
        cleaned = cleaned.replaceAll("(\\d)\\s*(cm|CM)(?=[\\s.,]|$)", "$1 xen ti mét");
        cleaned = cleaned.replaceAll("(\\d)\\s*(mm|MM)(?=[\\s.,]|$)", "$1 mi li mét");
        
        // 4. Xử lý mã sản phẩm (Dấu gạch dưới)
        cleaned = cleaned.replaceAll("([a-zA-Z0-9])_([a-zA-Z0-9])", "$1 $2");

        // 5. GIẢI PHÁP MẠNH: Chỉ giữ lại Chữ cái (bao gồm Tiếng Việt), Số, Dấu chấm, Dấu phẩy, Khoảng trắng
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ\\s.,]", " ");
        
        // 6. Loại bỏ khoảng trắng thừa
        String finalCleaned = cleaned.trim().replaceAll("\\s+", " ");
        
        // 7. KIỂM TRA QUAN TRỌNG: Nếu chuỗi chỉ toàn dấu câu (. ,) mà không có chữ/số nào -> FPT.AI sẽ lỗi
        if (!finalCleaned.matches(".*[a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ].*")) {
            return ""; // Trả về rỗng để bỏ qua
        }
        
        return finalCleaned;
    }
}
