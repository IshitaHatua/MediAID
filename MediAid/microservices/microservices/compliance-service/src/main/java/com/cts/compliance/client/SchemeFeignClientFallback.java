package com.cts.compliance.client;

import com.cts.compliance.dto.SchemeClientResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SchemeFeignClientFallback implements SchemeFeignClient {

    private static final Logger log = LoggerFactory.getLogger(SchemeFeignClientFallback.class);

    @Override
    public SchemeClientResponseDTO getScheme(Long schemeId) {
        log.warn("[ComplianceService] scheme-service unavailable — scheme enrichment skipped for schemeId={}. Rules will run on supplied context only.",
                schemeId);
        return null;
    }
}
