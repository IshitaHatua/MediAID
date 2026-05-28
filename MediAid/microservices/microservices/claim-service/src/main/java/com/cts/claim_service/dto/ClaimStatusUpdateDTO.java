package com.cts.claim_service.dto;

import com.cts.claim_service.model.Claim;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private Claim.ClaimStatus status;

    private String remarks;
}
