package com.cts.claim_service.client;

import com.cts.claim_service.api.APIResponse;
import com.cts.claim_service.dto.EnrollmentResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentClientFallback implements EnrollmentClient {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentClientFallback.class);

    @Override
    public Boolean validateEnrollment(Long citizenId, Long schemeId) {
        log.warn("[ClaimService] Enrollment Service unavailable — validateEnrollment citizenId={} schemeId={}", citizenId, schemeId);
        throw new RuntimeException("Enrollment Service is currently unavailable. Please try again later.");
    }

    @Override
    public String getEnrollmentStatus(Long citizenId, Long schemeId) {
        log.warn("[ClaimService] Enrollment Service unavailable — getEnrollmentStatus citizenId={} schemeId={}", citizenId, schemeId);
        throw new RuntimeException("Enrollment Service is currently unavailable. Please try again later.");
    }

    @Override
    public APIResponse<EnrollmentResponseDTO> getEnrollmentDetails(Long citizenId, Long schemeId) {
        log.warn("[ClaimService] Enrollment Service unavailable — getEnrollmentDetails citizenId={} schemeId={}", citizenId, schemeId);
        throw new RuntimeException("Enrollment Service is currently unavailable. Please try again later.");
    }
}
