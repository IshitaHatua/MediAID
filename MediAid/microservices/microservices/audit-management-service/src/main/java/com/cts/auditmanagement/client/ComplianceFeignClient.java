package com.cts.auditmanagement.client;

import com.cts.auditmanagement.dto.ComplianceEvaluationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Calls compliance-service's /api/compliance/evaluate endpoint.
 * Resolved via Eureka using spring.application.name=compliance-service.
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
