package com.cts.auditmanagement.serviceImpl;

import com.cts.auditmanagement.dto.AuditManagementLogRequest;
import com.cts.auditmanagement.model.AuditManagementLog;
import com.cts.auditmanagement.repository.AuditManagementLogRepository;
import com.cts.auditmanagement.service.AuditManagementLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditManagementLogServiceImpl implements AuditManagementLogService {

    private final AuditManagementLogRepository repository;

    public AuditManagementLogServiceImpl(AuditManagementLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditManagementLog createLog(AuditManagementLogRequest request) {
        // Safe null handling — defaults for every field
        LocalDateTime ts = (request.getTimestamp() != null)
                ? request.getTimestamp() : LocalDateTime.now();
        String action   = (request.getAction()   != null && !request.getAction().isBlank())
                ? request.getAction()   : "UNKNOWN";
        String resource = (request.getResource() != null && !request.getResource().isBlank())
                ? request.getResource() : "UNKNOWN";

        AuditManagementLog log = AuditManagementLog.builder()
                .userId(request.getUserId())
                .action(action)
                .resource(resource)
                .details(request.getDetails())
                .timestamp(ts)
                .build();

        return repository.save(log);
    }

    @Override
    public List<AuditManagementLog> getAllLogs() {
        return repository.findAll();
    }

    @Override
    public List<AuditManagementLog> getLatest100Logs() {
        return repository.findTop100ByOrderByTimestampDesc();
    }

    @Override
    public List<AuditManagementLog> getLogsByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<AuditManagementLog> getLogsByAction(String action) {
        return repository.findByAction(action);
    }

    @Override
    public List<AuditManagementLog> getLogsByResource(String resourceFragment) {
        return repository.findByResourceContaining(resourceFragment);
    }
}
