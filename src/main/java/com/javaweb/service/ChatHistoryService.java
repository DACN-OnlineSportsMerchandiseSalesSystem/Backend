package com.javaweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý lịch sử hội thoại ngắn hạn theo phiên (Session-based Short-term Memory).
 * Lưu trữ tối đa N lượt hỏi-đáp gần nhất vào Redis theo cơ chế Sliding Window.
 * Có cơ chế dự phòng lưu RAM (In-Memory Fallback) nếu Redis ngoại tuyến.
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
    private final Map<String, List<Turn>> memoryFallback = new ConcurrentHashMap<>();

    public ChatHistoryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /** Một lượt hội thoại (hỏi hoặc trả lời) */
    public record Turn(String role, String content) {}

    /** Lấy N lượt gần nhất dưới dạng chuỗi ghép vào Prompt */
    public String getHistoryAsContext(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return "";

        List<Turn> turns = new ArrayList<>();
        try {
            String key = KEY_PREFIX + sessionId;
            List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
            if (raw != null && !raw.isEmpty()) {
                for (String json : raw) {
                    try {
                        turns.add(objectMapper.readValue(json, Turn.class));
                    } catch (JsonProcessingException e) {
                        log.warn("Không đọc được lịch sử chat: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Redis không khả dụng ({}), chuyển sang bộ nhớ tạm RAM.", e.getMessage());
            turns = memoryFallback.getOrDefault(sessionId, List.of());
        }

        if (turns.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("[LỊCH SỬ HỘI THOẠI GẦN ĐÂY]\n");
        for (Turn turn : turns) {
            String label = "user".equals(turn.role()) ? "Khách hàng" : "SportBot";
            sb.append(label).append(": ").append(turn.content()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /** Lưu một lượt hội thoại (user hoặc bot) vào Redis và RAM */
    public void saveTurn(String sessionId, String role, String content) {
        if (sessionId == null || sessionId.isBlank()) return;

        Turn turn = new Turn(role, content);

        // 1. Luôn lưu vào bộ nhớ RAM dự phòng
        memoryFallback.compute(sessionId, (k, v) -> {
            List<Turn> list = (v != null) ? new ArrayList<>(v) : new ArrayList<>();
            list.add(turn);
            while (list.size() > MAX_TURNS * 2) {
                list.remove(0);
            }
            return list;
        });

        // 2. Thử lưu vào Redis nếu khả dụng
        try {
            String key = KEY_PREFIX + sessionId;
            String json = objectMapper.writeValueAsString(turn);
            redisTemplate.opsForList().rightPush(key, json);

            // Giới hạn Sliding Window: chỉ giữ MAX_TURNS * 2 phần tử (user + bot)
            Long size = redisTemplate.opsForList().size(key);
            if (size != null && size > MAX_TURNS * 2L) {
                redisTemplate.opsForList().leftPop(key);
                redisTemplate.opsForList().leftPop(key);
            }

            // Gia hạn TTL sau mỗi tin nhắn
            redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Lỗi khi lưu lịch sử chat vào Redis: {}", e.getMessage());
        }
    }

    /** Xóa toàn bộ lịch sử của session */
    public void clearHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;
        memoryFallback.remove(sessionId);
        try {
            redisTemplate.delete(KEY_PREFIX + sessionId);
        } catch (Exception e) {
            log.warn("Lỗi khi xóa lịch sử chat khỏi Redis: {}", e.getMessage());
        }
    }
}
