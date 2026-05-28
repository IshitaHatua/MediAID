package com.cts.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Audit Service.
 * Uses service name "audit-service" registered in Eureka — no hardcoded URL needed.
 */
@FeignClient(name = "audit-service", fallback = AuditFeignClientFallback.class)
public interface AuditFeignClient {

    @PostMapping("/api/audit/internal/log")
    void log(@RequestBody AuditLogRequest request);
}
