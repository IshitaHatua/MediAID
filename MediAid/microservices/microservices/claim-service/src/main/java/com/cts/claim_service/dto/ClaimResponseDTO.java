package com.cts.claim_service.dto;


import lombok.Getter;
import lombok.Setter;
import com.cts.claim_service.model.Claim.ClaimStatus;

@Getter
@Setter
public class ClaimResponseDTO {

    private Long claimId;
    private Long citizenId;
    private Long schemeId;
    private Double claimAmount;
    private String claimDate;
    private String description;
    private ClaimStatus status;
}
