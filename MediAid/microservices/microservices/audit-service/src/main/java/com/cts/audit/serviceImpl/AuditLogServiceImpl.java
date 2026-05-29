package com.cts.audit.serviceImpl;

import com.cts.audit.model.AuditLog;
import com.cts.audit.repository.AuditLogRepository;
import com.cts.audit.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AuditLog createLog(AuditLog log) {
        return auditLogRepository.save(log);
    }

    @Override
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    @Override
    public List<AuditLog> getLatest100Logs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    @Override
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
}
