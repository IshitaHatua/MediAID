package com.mediaid.payment.client;

import com.mediaid.payment.dto.response.DisbursementClientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DISBURSEMENT-SERVICE")
public interface DisbursementClient {

    @GetMapping("/api/disbursement/{disbursementId}")
    DisbursementClientResponseDTO getDisbursementById(@PathVariable Long disbursementId);

    @PatchMapping("/api/disbursement/{disbursementId}/status")
    void updateDisbursementStatus(
            @PathVariable Long disbursementId,
            @RequestParam("status") String status);
}
