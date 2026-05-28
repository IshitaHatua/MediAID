package com.cts.auditmanagement.service;

import com.cts.auditmanagement.dto.AuditManagementLogRequest;
import com.cts.auditmanagement.model.AuditManagementLog;

import java.util.List;

public interface AuditManagementLogService {
    AuditManagementLog createLog(AuditManagementLogRequest request);
    List<AuditManagementLog> getAllLogs();
    List<AuditManagementLog> getLatest100Logs();
    List<AuditManagementLog> getLogsByUser(Long userId);
    List<AuditManagementLog> getLogsByAction(String action);
    List<AuditManagementLog> getLogsByResource(String resourceFragment);
}
