package com.cts.compliance.client;

import com.cts.compliance.dto.ClaimClientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Fetches claim details from claim-service when compliance-service
 * receives only an entityId for a CLAIM evaluation (simple /evaluate endpoint).
 * Used to populate amount, claimType, and policy expiry into the rule engine.
 *
 * Eureka name must match claim-service spring.application.name = claim-service
 */
@FeignClient(name = "claim-service", fallback = ClaimFeignClientFallback.class)
public interface ClaimFeignClient {

    @GetMapping("/api/claims/internal/{claimId}")
    ClaimClientResponseDTO getClaim(@PathVariable("claimId") Long claimId);
}
