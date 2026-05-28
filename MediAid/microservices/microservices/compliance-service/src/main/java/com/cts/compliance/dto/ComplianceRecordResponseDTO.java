package com.cts.compliance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceRecordResponseDTO {
    private Long complianceId;
    private Long entityId;
    private String entityType;
    private String result;
    private String notes;
    private Long requestedBy;
    private LocalDateTime evaluatedAt;
}
