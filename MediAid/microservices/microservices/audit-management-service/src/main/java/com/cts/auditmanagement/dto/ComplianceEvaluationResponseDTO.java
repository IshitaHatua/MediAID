package com.cts.auditmanagement.dto;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ComplianceEvaluationResponseDTO {
    private Long complianceRecordId;
    private String result;
    private String notes;
}
