package com.mediaid.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class PaymentRequestDTO {

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "Credit Card|PayPal|Bank Transfer",
             message = "Payment method must be Credit Card, PayPal, or Bank Transfer")
    private String method;

    @NotNull(message = "Payment Date is Required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDateTime date;

    @NotBlank(message = "Payment status is required")
    @Pattern(regexp = "Pending|Completed|Failed",
             message = "Payment status must be Pending, Completed, or Failed")
    private String status;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    @DecimalMax(value = "1000000.0", message = "Amount must be less than or equal to 1,000,000")
    private BigDecimal amount;

    @NotNull(message = "Disbursement ID is required")
    private Long disbursementId;
}
