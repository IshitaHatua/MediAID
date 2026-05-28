package com.cts.compliance.dto;

import lombok.*;

/**
 * Returned to audit-service (via Feign) and to direct callers (via /evaluate/full).
 * result is always a non-null string: PASS, FAIL, or FLAGGED.
 * notes is always a non-null string describing which rules passed or failed.
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
