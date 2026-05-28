package com.mediaid.payment.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class PaymentResponseDTO {
    private Long paymentId;
    private String method;
    private LocalDateTime date;
    private String status;
    private BigDecimal amount;
    private Long disbursementId;
    private Long citizenId;
}
