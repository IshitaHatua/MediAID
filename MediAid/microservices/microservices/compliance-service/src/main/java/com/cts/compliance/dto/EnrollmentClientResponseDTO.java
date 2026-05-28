package com.cts.compliance.dto;

import lombok.*;

/**
 * Local copy of the response shape returned by enrollment-service
 * from GET /api/enrollments/{id}.
 *
 * Mirrors: APIResponse<EnrollmentResponseDTO> { status, message, data }
 *
 * Fields match EnrollmentResponseDTO exactly:
 *   enrollmentId, citizenId, schemeId, enrollmentDate, expiryDate, status
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentClientResponseDTO {
    private String status;
    private String message;
    private EnrollmentData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EnrollmentData {
        private Long enrollmentId;
        private Long citizenId;
        private Long schemeId;
        private String enrollmentDate;  // "YYYY-MM-DD" string
        private String expiryDate;      // "YYYY-MM-DD" string
        private String status;          // PENDING, APPROVED, REJECTED, ACTIVE
    }
}
