package com.mediaid.payment.client;

import com.mediaid.payment.dto.response.DisbursementClientResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DisbursementClientFallback implements DisbursementClient {

    @Override
    public DisbursementClientResponseDTO getDisbursementById(Long disbursementId) {
        log.warn("[PaymentService] Disbursement Service unavailable — getDisbursementById disbursementId={}", disbursementId);
        throw new RuntimeException("Disbursement Service is currently unavailable. Please try again later.");
    }

    @Override
    public void updateDisbursementStatus(Long disbursementId, String status) {
        log.warn("[PaymentService] Disbursement Service unavailable — updateDisbursementStatus disbursementId={}", disbursementId);
    }
}
