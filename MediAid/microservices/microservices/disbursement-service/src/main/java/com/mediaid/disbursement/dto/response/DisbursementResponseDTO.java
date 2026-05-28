package com.mediaid.disbursement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class DisbursementResponseDTO {

    private Long disbursementId;
    private BigDecimal amount;
    private LocalDateTime date;
    private String status;
    private Long claimId;
    private Long citizenId;
    private Long schemeId;
}
