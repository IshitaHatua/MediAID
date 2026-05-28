package com.cts.compliance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Local copy of the response shape returned by disbursement-service
 * from GET /api/disbursement/{disbursementId}.
 *
 * Mirrors: APIResponse<DisbursementResponseDTO> { status, message, data }
 *
 * Fields match DisbursementResponseDTO exactly:
 *   disbursementId, amount, date, status, claimId, citizenId, schemeId
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementClientResponseDTO {
    private String status;
    private String message;
    private DisbursementData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DisbursementData {
        private Long disbursementId;
        private BigDecimal amount;
        private LocalDateTime date;
        private String status;
        private Long claimId;
        private Long citizenId;
        private Long schemeId;
    }
}
