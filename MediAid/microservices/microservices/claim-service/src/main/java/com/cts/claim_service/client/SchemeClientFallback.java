package com.cts.claim_service.client;

import com.cts.claim_service.api.APIResponse;
import com.cts.claim_service.dto.SchemeResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SchemeClientFallback implements SchemeClient {

    private static final Logger log = LoggerFactory.getLogger(SchemeClientFallback.class);

    @Override
    public APIResponse<SchemeResponseDTO> getSchemeById(Long schemeId) {
        log.warn("[ClaimService] Scheme Service unavailable — getSchemeById schemeId={}", schemeId);
        throw new RuntimeException("Scheme Service is currently unavailable. Please try again later.");
    }
}
