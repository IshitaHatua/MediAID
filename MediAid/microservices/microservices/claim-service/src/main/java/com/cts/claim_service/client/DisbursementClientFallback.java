package com.cts.claim_service.client;

import com.cts.claim_service.dto.DisbursementCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DisbursementClientFallback implements DisbursementClient {

    private static final Logger log = LoggerFactory.getLogger(DisbursementClientFallback.class);

    @Override
    public BigDecimal getTotalUtilized(List<Long> claimIds) {
        log.warn("[ClaimService] Disbursement Service unavailable — getTotalUtilized, returning ZERO");
        return BigDecimal.ZERO;
    }

    @Override
    public Object createDisbursement(DisbursementCreateRequest req) {
        log.warn("[ClaimService] Disbursement Service unavailable — createDisbursement claimId={}",
                req != null ? req.getClaimId() : "null");
        return null;
    }
}
