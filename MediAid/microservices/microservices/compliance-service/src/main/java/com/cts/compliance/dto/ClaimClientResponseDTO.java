package com.cts.compliance.dto;

import lombok.*;

/**
 * Local copy of the response shape returned by claim-service
 * from GET /api/claims/{claimId}.
 *
 * Mirrors: APIResponse<ClaimResponseDTO> { status, message, data: ClaimResponseDTO }
 *
 * Fields match exactly what ClaimResponseDTO exposes:
 *   claimId, citizenId, schemeId, claimAmount, claimDate, description, status
 *
 * claimType is NOT present in the existing Claim entity — it is a planned
 * future field. When it is added, add it here too.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimClientResponseDTO {
    private String status;
    private String message;
    private ClaimData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClaimData {
        private Long claimId;
        private Long citizenId;
        private Long schemeId;
        private Double claimAmount;
        private String claimDate;
        private String description;
        private String status;
        // claimType not present yet — planned future field on Claim entity
    }
}
