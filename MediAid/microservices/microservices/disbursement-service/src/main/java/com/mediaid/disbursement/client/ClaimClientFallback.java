package com.mediaid.disbursement.client;

import com.mediaid.disbursement.dto.response.ClaimClientResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClaimClientFallback implements ClaimClient {

    @Override
    public ClaimClientResponseDTO getClaim(Long claimId) {
        log.warn("[DisbursementService] Claim Service unavailable — getClaim claimId={}", claimId);
        throw new RuntimeException("Claim Service is currently unavailable. Please try again later.");
    }
}
