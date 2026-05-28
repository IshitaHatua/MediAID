package com.cts.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditFeignClientFallback implements AuditFeignClient {

    private static final Logger log = LoggerFactory.getLogger(AuditFeignClientFallback.class);

    @Override
    public void log(AuditLogRequest request) {
        log.warn("[Citizen] Audit logging failed (audit-service unavailable) - action={} userId={}",
                request != null ? request.getAction() : "null",
                request != null ? request.getUserId() : "null");
    }
}
