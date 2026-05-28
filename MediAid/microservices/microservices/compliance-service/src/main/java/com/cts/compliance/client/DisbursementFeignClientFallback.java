package com.cts.compliance.client;

import com.cts.compliance.dto.DisbursementClientResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DisbursementFeignClientFallback implements DisbursementFeignClient {

    private static final Logger log = LoggerFactory.getLogger(DisbursementFeignClientFallback.class);

    @Override
    public DisbursementClientResponseDTO getDisbursement(Long disbursementId) {
        log.warn("[ComplianceService] disbursement-service unavailable — enrichment skipped for disbursementId={}.",
                disbursementId);
        return null;
    }
}
