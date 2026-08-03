package com.javaweb.service.impl;

import com.javaweb.entity.AuditLog;
import com.javaweb.repository.AuditLogRepository;
import com.javaweb.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void recordLog(String actorEmail, String action, String targetType, String targetId, String oldValue, String newValue, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setActorEmail(actorEmail != null ? actorEmail : "SYSTEM");
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByIdDesc();
    }
}
