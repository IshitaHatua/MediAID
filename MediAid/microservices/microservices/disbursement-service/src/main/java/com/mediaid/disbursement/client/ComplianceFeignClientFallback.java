package com.mediaid.disbursement.client;

import com.mediaid.disbursement.dto.ComplianceEvaluationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ComplianceFeignClientFallback implements ComplianceFeignClient {

    private static final Logger log = LoggerFactory.getLogger(ComplianceFeignClientFallback.class);

    @Override
    public ComplianceEvaluationResponseDTO evaluate(Long entityId, String entityType, Long requestedBy) {
        log.warn("[DisbursementService] compliance-service unavailable — auto-evaluation skipped for {}:{}.",
                entityType, entityId);
        return ComplianceEvaluationResponseDTO.builder()
                .result("FLAGGED")
                .notes("Compliance service unavailable at disbursement creation. Manual evaluation required.")
                .build();
    }
}
