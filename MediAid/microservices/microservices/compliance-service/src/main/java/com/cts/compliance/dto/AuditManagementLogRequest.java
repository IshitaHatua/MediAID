package com.cts.compliance.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Local copy of the DTO used by compliance-service to write log entries
 * into audit-management-service via POST /api/audit-management/internal/log.
 *
 * This is a deliberate local copy — services share contracts, not code.
 * Must stay in sync with AuditManagementLogRequest in audit-management-service.
 */
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AuditManagementLogRequest {
    private Long userId;
    private String action;
    private String resource;
    private String details;
    private LocalDateTime timestamp;
}
