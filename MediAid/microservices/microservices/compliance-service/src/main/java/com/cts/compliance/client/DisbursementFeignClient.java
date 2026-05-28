package com.cts.compliance.client;

import com.cts.compliance.dto.DisbursementClientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Fetches disbursement details from disbursement-service when compliance-service
 * receives only an entityId for a DISBURSEMENT evaluation (simple /evaluate endpoint).
 * Used to populate amount and linkedClaimId into the rule engine.
 *
 * Eureka name must match: spring.application.name = disbursement-service
 */
@FeignClient(name = "disbursement-service", fallback = DisbursementFeignClientFallback.class)
public interface DisbursementFeignClient {

    @GetMapping("/api/disbursement/{disbursementId}")
    DisbursementClientResponseDTO getDisbursement(@PathVariable("disbursementId") Long disbursementId);
}
