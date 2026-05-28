package com.cts.claim_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "DISBURSEMENT-SERVICE")
public interface DisbursementClient {

    // Returns sum of COMPLETED disbursement amounts for the given claim IDs.
    // Returns 0 when none found — no 404 risk.
    @GetMapping("/api/disbursement/utilized")
    BigDecimal getTotalUtilized(@RequestParam List<Long> claimIds);
}
