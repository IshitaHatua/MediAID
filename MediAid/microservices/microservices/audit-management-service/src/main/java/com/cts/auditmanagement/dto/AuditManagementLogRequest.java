package com.cts.auditmanagement.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Used by compliance-service (via Feign) to write an activity entry
 * into this service's audit_management_log table.
 * Endpoint: POST /api/audit-management/internal/log
 */
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AuditManagementLogRequest {
    private Long userId;
    private String action;
    private String resource;
    private String details;
    private LocalDateTime timestamp;
}
