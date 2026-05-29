package com.cts.claim_service.client;

import com.cts.claim_service.dto.DisbursementCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "DISBURSEMENT-SERVICE")
public interface DisbursementClient {

    // Returns sum of COMPLETED disbursement amounts for the given claim IDs.
    // Returns 0 when none found — no 404 risk.
    @GetMapping("/api/disbursement/utilized")
    BigDecimal getTotalUtilized(@RequestParam List<Long> claimIds);

    // Auto-creates a disbursement when a claim is APPROVED.
    // Officer-only on the disbursement side; the FeignAuthInterceptor forwards
    // the approving officer's X-User-Id / X-User-Role headers so PreAuthorize passes.
    // Return type is Object — claim-service doesn't need to consume the response body.
    @PostMapping("/api/disbursement")
    Object createDisbursement(@RequestBody DisbursementCreateRequest req);
}
