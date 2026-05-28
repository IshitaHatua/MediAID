package com.mediaid.disbursement.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DisbursementRequestDTO {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    @DecimalMax(value = "1000000.0", message = "Amount must be less than or equal to 1,000,000")
    private BigDecimal amount;

    @NotNull(message = "Disbursement date is required")
    @PastOrPresent(message = "Disbursement date cannot be in the future")
    private LocalDateTime date;

    @NotBlank(message = "Disbursement status is required")
    @Pattern(regexp = "Pending|Processing|Completed|Failed",
             message = "Disbursement status must be Pending, Processing, Completed, or Failed")
    private String status;

    @NotNull(message = "Claim ID is required")
    private Long claimId;
}
