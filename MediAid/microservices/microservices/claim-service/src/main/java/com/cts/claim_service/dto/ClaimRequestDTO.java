package com.cts.claim_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimRequestDTO {

    @NotNull(message = "Scheme ID is required")
    private Long schemeId;

    @NotNull(message = "Claim amount is required")
    private Double claimAmount;
}