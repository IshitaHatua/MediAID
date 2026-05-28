package com.mediaid.disbursement.dto;

import lombok.*;

/**
 * Local copy of the response returned by compliance-service
 * from POST /api/compliance/evaluate.
 * result is always: PASS, FAIL, or FLAGGED.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceEvaluationResponseDTO {
    private Long complianceRecordId;
    private String result;
    private String notes;
}
