package com.javaweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý lịch sử hội thoại ngắn hạn theo phiên (Session-based Short-term Memory).
 * Lưu trữ tối đa N lượt hỏi-đáp gần nhất vào Redis theo cơ chế Sliding Window.
 * TTL mặc định: 30 phút kể từ tin nhắn cuối.
 */
@Service
public class ChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);
    private static final String KEY_PREFIX = "chat:session:";
    private static final int MAX_TURNS = 5;        // Số lượt hỏi-đáp tối đa giữ lại
    private static final long TTL_MINUTES = 30;    // Thời gian sống của session

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatHistoryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /** Một lượt hội thoại (hỏi hoặc trả lời) */
    public record Turn(String role, String content) {}

    /** Lấy N lượt gần nhất dưới dạng chuỗi ghép vào Prompt */
    public String getHistoryAsContext(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return "";

        String key = KEY_PREFIX + sessionId;
        List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("[LỊCH SỬ HỘI THOẠI GẦN ĐÂY]\n");
        for (String json : raw) {
            try {
                Turn turn = objectMapper.readValue(json, Turn.class);
                String label = "user".equals(turn.role()) ? "Khách hàng" : "SportBot";
                sb.append(label).append(": ").append(turn.content()).append("\n");
            } catch (JsonProcessingException e) {
                log.warn("Không đọc được lịch sử chat: {}", e.getMessage());
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    /** Lưu một lượt hội thoại (user hoặc bot) vào Redis */
    public void saveTurn(String sessionId, String role, String content) {
        if (sessionId == null || sessionId.isBlank()) return;

        String key = KEY_PREFIX + sessionId;
        try {
            String json = objectMapper.writeValueAsString(new Turn(role, content));
            redisTemplate.opsForList().rightPush(key, json);

            // Giới hạn Sliding Window: chỉ giữ MAX_TURNS * 2 phần tử (user + bot)
            Long size = redisTemplate.opsForList().size(key);
            if (size != null && size > MAX_TURNS * 2L) {
                redisTemplate.opsForList().leftPop(key);
                redisTemplate.opsForList().leftPop(key);
            }

            // Gia hạn TTL sau mỗi tin nhắn
            redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi lưu lịch sử chat: {}", e.getMessage());
        }
    }

    /** Xóa toàn bộ lịch sử của session */
    public void clearHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;
        redisTemplate.delete(KEY_PREFIX + sessionId);
    }
}
