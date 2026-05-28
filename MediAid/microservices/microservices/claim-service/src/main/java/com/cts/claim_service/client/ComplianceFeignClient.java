package com.cts.claim_service.client;

import com.cts.claim_service.dto.ComplianceEvaluationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Triggers compliance evaluation in compliance-service after a claim is APPROVED.
 * Endpoint is permitAll in compliance-service — no JWT forwarding needed.
 * Only called when status changes to APPROVED, never for REJECTED.
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
