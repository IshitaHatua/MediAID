package com.cts.enrollment_service.client;

import com.cts.enrollment_service.dto.ComplianceEvaluationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Triggers POLICY compliance evaluation in compliance-service after an
 * enrollment is APPROVED. Compliance-service fetches expiry/enrollment
 * dates and citizenId from enrollment-service via EnrollmentFeignClient
 * to run P-1, P-2, P-3 rules.
 * Endpoint is permitAll in compliance-service — no JWT forwarding needed.
 */
@FeignClient(name = "compliance-service", fallback = ComplianceFeignClientFallback.class)
public interface ComplianceFeignClient {

    @PostMapping("/api/compliance/evaluate")
    ComplianceEvaluationResponseDTO evaluate(
            @RequestParam("entityId")    Long   entityId,
            @RequestParam("entityType")  String entityType,
            @RequestParam("requestedBy") Long   requestedBy
    );
}
