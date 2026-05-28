package com.cts.compliance.client;

import com.cts.compliance.dto.EnrollmentClientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Fetches enrollment details from enrollment-service when compliance-service
 * receives only an entityId for a POLICY evaluation (simple /evaluate endpoint).
 * Used to populate policyExpiryDate, policyEnrollmentDate, and citizenId
 * into the rule engine.
 *
 * Calls GET /api/enrollments/{id} — enrollment-service exposes this endpoint.
 * Eureka name must match: spring.application.name = enrollment-service
 */
@FeignClient(name = "enrollment-service", fallback = EnrollmentFeignClientFallback.class)
public interface EnrollmentFeignClient {

    @GetMapping("/api/enrollments/{id}")
    EnrollmentClientResponseDTO getEnrollment(
            @org.springframework.web.bind.annotation.PathVariable("id") Long enrollmentId);
}
