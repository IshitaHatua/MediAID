package com.cts.claim_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemeResponseDTO {

    private Long schemeId;
    private String name;
    private Double maxCoverageAmount;
    private int validityYears;
    private String status;
}
