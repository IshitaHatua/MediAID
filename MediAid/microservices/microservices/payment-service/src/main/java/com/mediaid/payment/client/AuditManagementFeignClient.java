package com.mediaid.payment.client;

import com.mediaid.payment.dto.AuditManagementLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Writes activity entries into audit-management-service after payment events.
 * Called after payment creation and after auto-disbursement completion.
 * Endpoint is permitAll in audit-management-service — no JWT forwarding needed.
 */
@FeignClient(name = "audit-management-service", fallback = AuditManagementFeignClientFallback.class)
public interface AuditManagementFeignClient {

    @PostMapping("/api/audit-management/internal/log")
    void log(@RequestBody AuditManagementLogRequest request);
}
