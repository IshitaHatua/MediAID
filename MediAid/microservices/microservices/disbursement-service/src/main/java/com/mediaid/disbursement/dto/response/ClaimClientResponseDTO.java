package com.mediaid.disbursement.dto.response;

import lombok.Data;

@Data
public class ClaimClientResponseDTO {

    private String status;
    private String message;
    private ClaimData data;

    @Data
    public static class ClaimData {
        private Long claimId;
        private Long citizenId;
        private Long schemeId;
        private Double claimAmount;
        private String claimDate;
        private String description;
        private String status;
    }
}
