package com.cts.scheme.client;

import com.cts.scheme.dto.AuditManagementLogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback for AuditManagementFeignClient.
 * Audit logging is best-effort — scheme operations always complete regardless
 * of audit-management-service availability.
 */
@Component
public class AuditManagementFeignClientFallback implements AuditManagementFeignClient {

    private static final Logger log = LoggerFactory.getLogger(AuditManagementFeignClientFallback.class);

    @Override
    public void log(AuditManagementLogRequest request) {
        log.warn("[SchemeService] audit-management-service unavailable — audit log NOT written. action={} resource={}",
                request != null ? request.getAction() : "null",
                request != null ? request.getResource() : "null");
    }
}
