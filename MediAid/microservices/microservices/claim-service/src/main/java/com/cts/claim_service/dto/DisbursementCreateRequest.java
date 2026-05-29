package com.cts.claim_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outbound DTO used by claim-service to auto-create a disbursement in
 * disbursement-service when an officer approves a claim. Must remain shape-
 * compatible with disbursement-service's DisbursementRequestDTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursementCreateRequest {
    private BigDecimal amount;
    private LocalDateTime date;
    private String status;
    private Long claimId;
}
