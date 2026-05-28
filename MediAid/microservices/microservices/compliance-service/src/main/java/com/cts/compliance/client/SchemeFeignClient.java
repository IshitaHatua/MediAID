package com.cts.compliance.client;

import com.cts.compliance.dto.SchemeClientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Fetches scheme details from scheme-service when compliance-service
 * needs to verify maxCoverageAmount, validityYears, and status for a
 * CLAIM, POLICY, or DISBURSEMENT evaluation.
 *
 * Eureka name must match scheme-service spring.application.name = scheme-service
 */
@FeignClient(name = "scheme-service", fallback = SchemeFeignClientFallback.class)
public interface SchemeFeignClient {

    @GetMapping("/api/schemes/internal/{schemeId}")
    SchemeClientResponseDTO getScheme(@PathVariable("schemeId") Long schemeId);
}
