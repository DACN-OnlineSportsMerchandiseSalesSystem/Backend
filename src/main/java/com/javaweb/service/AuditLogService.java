package com.javaweb.service;

import com.javaweb.entity.AuditLog;
import java.util.List;

public interface AuditLogService {
    void recordLog(String actorEmail, String action, String targetType, String targetId, String oldValue, String newValue, String ipAddress);
    List<AuditLog> getAllLogs();
}
