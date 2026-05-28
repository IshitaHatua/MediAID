package com.cts.auditmanagement.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class FormalAuditResponseDTO {
    private Long auditId;
    private Long officerId;
    private String scope;
    private Long scopeEntityId;
    private String findings;
    private LocalDateTime createdAt;
    private String status;
}
