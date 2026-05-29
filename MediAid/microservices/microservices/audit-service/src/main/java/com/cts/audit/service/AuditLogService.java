package com.cts.audit.service;

import com.cts.audit.model.AuditLog;

import java.util.List;

public interface AuditLogService {

    AuditLog createLog(AuditLog log);

    List<AuditLog> getAllLogs();

    List<AuditLog> getLatest100Logs();

    List<AuditLog> getLogsByUser(Long userId);
}
