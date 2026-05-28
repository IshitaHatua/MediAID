package com.cts.scheme.client;

import com.cts.scheme.dto.AuditManagementLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Writes activity entries into audit-management-service after scheme events.
 * Called after scheme create, status update, and delete.
 * Endpoint is permitAll in audit-management-service — no JWT forwarding needed.
 */
@FeignClient(name = "audit-management-service", fallback = AuditManagementFeignClientFallback.class)
public interface AuditManagementFeignClient {

    @PostMapping("/api/audit-management/internal/log")
    void log(@RequestBody AuditManagementLogRequest request);
}
