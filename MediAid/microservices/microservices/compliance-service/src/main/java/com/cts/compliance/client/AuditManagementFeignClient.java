package com.cts.compliance.client;

import com.cts.compliance.dto.AuditManagementLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Writes activity entries into audit-management-service after every compliance evaluation.
 *
 * IMPORTANT: This client points to "audit-management-service" (port 8091),
 * NOT to "audit-service" (port 8083). The existing audit-service is a separate
 * service owned by another team member. Your service communicates only with
 * audit-management-service.
 *
 * The endpoint /api/audit-management/internal/log is permitAll in
 * audit-management-service's SecurityConfig — no JWT forwarding needed.
 */
@FeignClient(name = "audit-management-service", fallback = AuditManagementFeignClientFallback.class)
public interface AuditManagementFeignClient {

    @PostMapping("/api/audit-management/internal/log")
    void log(@RequestBody AuditManagementLogRequest request);
}
