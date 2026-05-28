package com.cts.enrollment_service.client;

import com.cts.enrollment_service.dto.AuditManagementLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Writes activity entries into audit-management-service after enrollment events.
 * Called after enrollment submission and after status changes (APPROVED/REJECTED).
 * Endpoint is permitAll in audit-management-service — no JWT forwarding needed.
 */
@FeignClient(name = "audit-management-service", fallback = AuditManagementFeignClientFallback.class)
public interface AuditManagementFeignClient {

    @PostMapping("/api/audit-management/internal/log")
    void log(@RequestBody AuditManagementLogRequest request);
}
