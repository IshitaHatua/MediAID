package com.mediaid.payment.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Local copy of the DTO used to write log entries to audit-management-service.
 */
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AuditManagementLogRequest {
    private Long userId;
    private String action;
    private String resource;
    private String details;
    private LocalDateTime timestamp;
}
