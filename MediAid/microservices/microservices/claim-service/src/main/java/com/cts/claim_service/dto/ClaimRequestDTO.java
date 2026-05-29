package com.cts.claim_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimRequestDTO {

    @NotNull(message = "Scheme ID is required")
    private Long schemeId;

    @NotNull(message = "Claim amount is required")
    private Double claimAmount;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}