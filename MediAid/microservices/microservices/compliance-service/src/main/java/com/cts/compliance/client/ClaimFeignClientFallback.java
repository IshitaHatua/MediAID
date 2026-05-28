package com.cts.compliance.client;

import com.cts.compliance.dto.ClaimClientResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClaimFeignClientFallback implements ClaimFeignClient {

    private static final Logger log = LoggerFactory.getLogger(ClaimFeignClientFallback.class);

    @Override
    public ClaimClientResponseDTO getClaim(Long claimId) {
        log.warn("[ComplianceService] claim-service unavailable — enrichment skipped for claimId={}. Rules will run on supplied context only.",
                claimId);
        return null;
    }
}
