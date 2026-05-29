package com.cts.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceClient.class);

    private final AuditFeignClient auditFeignClient;

    public AuditServiceClient(AuditFeignClient auditFeignClient) {
        this.auditFeignClient = auditFeignClient;
    }

    public void log(Long userId, String action, String resource) {
        auditFeignClient.log(new AuditLogRequest(userId, action, resource));
        log.debug("[Citizen] Audit log written: action={} userId={}", action, userId);
    }
}
