package com.mediaid.payment.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisbursementClientResponseDTO {
    private String status;
    private String message;
    private DisbursementData data;

    @Data
    public static class DisbursementData {
        private Long disbursementId;
        private BigDecimal amount;
        private LocalDateTime date;
        private String status;
        private Long claimId;
        private Long citizenId;
        private Long schemeId;
    }
}
