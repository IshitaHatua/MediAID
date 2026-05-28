package com.cts.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Wrapper around AuditFeignClient.
 * Provides a simple log() method used throughout auth-service.
 * Uses Feign + Eureka — no hardcoded URLs, automatic load balancing.
 */
@Component
public class AuditServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceClient.class);

    private final AuditFeignClient auditFeignClient;

    public AuditServiceClient(AuditFeignClient auditFeignClient) {
        this.auditFeignClient = auditFeignClient;
    }

    public void log(Long userId, String action, String resource) {
        try {
            AuditLogRequest request = new AuditLogRequest(userId, action, resource);
            auditFeignClient.log(request);
            log.debug("[Auth] Audit log written: action={} userId={}", action, userId);
        } catch (Exception e) {
            log.warn("[Auth] Audit logging failed (non-critical): {}", e.getMessage());
        }
    }

    /** Overload that also records a details string for richer audit trail entries. */
    public void log(Long userId, String action, String resource, String details) {
        try {
            AuditLogRequest request = new AuditLogRequest(userId, action, resource, details);
            auditFeignClient.log(request);
            log.debug("[Auth] Audit log written: action={} userId={}", action, userId);
        } catch (Exception e) {
            log.warn("[Auth] Audit logging failed (non-critical): {}", e.getMessage());
        }
    }
}
