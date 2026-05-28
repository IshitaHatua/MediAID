package com.cts.auditmanagement.client;

import com.cts.auditmanagement.dto.ComplianceEvaluationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ComplianceFeignClientFallback implements ComplianceFeignClient {

    private static final Logger log = LoggerFactory.getLogger(ComplianceFeignClientFallback.class);

    @Override
    public ComplianceEvaluationResponseDTO evaluate(Long entityId, String entityType, Long requestedBy) {
        log.warn("[AuditManagementService] compliance-service unavailable — entityId={} entityType={}",
                entityId, entityType);
        return ComplianceEvaluationResponseDTO.builder()
                .result("FLAGGED")
                .notes("Compliance service unavailable. Evaluation pending — please retry.")
                .build();
    }
}
