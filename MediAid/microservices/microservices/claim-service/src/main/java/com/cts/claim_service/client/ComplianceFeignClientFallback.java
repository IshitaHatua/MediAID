package com.cts.claim_service.client;

import com.cts.claim_service.dto.ComplianceEvaluationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback for ComplianceFeignClient. Claim approval always completes —
 * compliance evaluation is best-effort. If compliance-service is down, the
 * claim stays APPROVED and an officer can trigger evaluation manually.
 */
@Component
public class ComplianceFeignClientFallback implements ComplianceFeignClient {

    private static final Logger log = LoggerFactory.getLogger(ComplianceFeignClientFallback.class);

    @Override
    public ComplianceEvaluationResponseDTO evaluate(Long entityId, String entityType, Long requestedBy) {
        log.warn("[ClaimService] compliance-service unavailable — auto-evaluation skipped for {}:{}.",
                entityType, entityId);
        return ComplianceEvaluationResponseDTO.builder()
                .result("FLAGGED")
                .notes("Compliance service unavailable at claim approval. Manual evaluation required.")
                .build();
    }
}
