package com.cts.compliance.dto;

import lombok.*;

/**
 * Local copy of the response shape returned by scheme-service
 * from GET /api/schemes/{schemeId}.
 *
 * Mirrors: APIResponse<SchemeResponseDTO> { status, message, data: SchemeResponseDTO }
 *
 * Fields match exactly what SchemeResponseDTO exposes:
 *   schemeId, name, maxCoverageAmount, validityYears, status
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemeClientResponseDTO {
    private String status;
    private String message;
    private SchemeData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SchemeData {
        private Long schemeId;
        private String name;
        private Double maxCoverageAmount;
        private int validityYears;
        private String status;
    }
}
