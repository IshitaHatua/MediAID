package com.cts.compliance.client;

import com.cts.compliance.dto.EnrollmentClientResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentFeignClientFallback implements EnrollmentFeignClient {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentFeignClientFallback.class);

    @Override
    public EnrollmentClientResponseDTO getEnrollment(Long enrollmentId) {
        log.warn("[ComplianceService] enrollment-service unavailable — enrichment skipped for enrollmentId={}.",
                enrollmentId);
        return null;
    }
}
