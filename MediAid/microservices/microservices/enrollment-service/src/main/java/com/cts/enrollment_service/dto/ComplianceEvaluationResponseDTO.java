package com.cts.enrollment_service.dto;

import lombok.*;

/**
 * Local copy of the response from POST /api/compliance/evaluate.
 * result: PASS, FAIL, or FLAGGED.
 */
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ComplianceEvaluationResponseDTO {
    private Long complianceRecordId;
    private String result;
    private String notes;
}
