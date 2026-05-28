package com.cts.user.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback for AuditFeignClient.
 * Called automatically by Resilience4j when audit-service is down or times out.
 * User operations continue normally — audit logging is non-critical.
 */
@Component
public class AuditFeignClientFallback implements AuditFeignClient {

    private static final Logger log = LoggerFactory.getLogger(AuditFeignClientFallback.class);

    @Override
    public void log(AuditLogRequest request) {
        log.warn("[User] Audit logging failed (audit-service unavailable) - action={} userId={}",
                request != null ? request.getAction() : "null",
                request != null ? request.getUserId() : "null");
    }
}
