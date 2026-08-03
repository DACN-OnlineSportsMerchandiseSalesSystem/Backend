package com.javaweb.controller;

import com.javaweb.entity.AuditLog;
import com.javaweb.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@Tag(name = "System Administration", description = "Endpoints for IT Admins to monitor system health, view audit logs, and clear cache.")
@PreAuthorize("hasRole('IT_ADMIN')")
public class SystemController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final ElasticsearchOperations elasticsearchOperations;
    private final AuditLogService auditLogService;

    @Value("${chroma.base-url:http://localhost:8000/}")
    private String chromaBaseUrl;

    @GetMapping("/health")
    @Operation(summary = "Get system health details", description = "IT Admin only. Verifies status of Database, Redis, Elasticsearch, and ChromaDB.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved system health details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires IT_ADMIN role")
    })
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        // 1. Database Health
        String dbStatus;
        try (Connection conn = dataSource.getConnection()) {
            dbStatus = conn.isValid(1) ? "UP" : "DOWN";
        } catch (Exception e) {
            dbStatus = "DOWN (" + e.getMessage() + ")";
        }
        health.put("database", dbStatus);

        // 2. Redis Health
        String redisStatus;
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            redisStatus = "PONG".equalsIgnoreCase(pong) ? "UP" : "DOWN (" + pong + ")";
        } catch (Exception e) {
            redisStatus = "DOWN (" + e.getMessage() + ")";
        }
        health.put("redis", redisStatus);

        // 3. Elasticsearch Health
        String esStatus;
        try {
            boolean exists = elasticsearchOperations.indexOps(com.javaweb.document.ProductDocument.class).exists();
            esStatus = "UP (Index ready: " + exists + ")";
        } catch (Exception e) {
            esStatus = "DOWN (" + e.getMessage() + ")";
        }
        health.put("elasticsearch", esStatus);

        // 4. ChromaDB Health
        String chromaStatus;
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            Object response = restTemplate.getForObject(chromaBaseUrl + "api/v1/heartbeat", Object.class);
            chromaStatus = response != null ? "UP (Heartbeat: " + response + ")" : "DOWN (Empty response)";
        } catch (Exception e) {
            chromaStatus = "DOWN (" + e.getMessage() + ")";
        }
        health.put("chroma", chromaStatus);

        return ResponseEntity.ok(health);
    }

    @PostMapping("/clear-cache")
    @Operation(summary = "Clear Redis cache keys", description = "IT Admin only. Flushes all cached keys in the Redis database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache cleared successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires IT_ADMIN role"),
        @ApiResponse(responseCode = "500", description = "Failed to clear cache")
    })
    public ResponseEntity<String> clearCache(HttpServletRequest request) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushDb();

            String currentActorEmail = SecurityContextHolder.getContext().getAuthentication() != null
                    ? SecurityContextHolder.getContext().getAuthentication().getName()
                    : "SYSTEM";

            auditLogService.recordLog(
                    currentActorEmail,
                    "CLEAR_CACHE",
                    "SYSTEM",
                    "N/A",
                    "N/A",
                    "ALL CACHE PURGED",
                    request.getRemoteAddr()
            );

            return ResponseEntity.ok("Cache cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to clear cache: " + e.getMessage());
        }
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get system audit logs", description = "IT Admin only. Retrieve chronological list of administrative actions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires IT_ADMIN role")
    })
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}
