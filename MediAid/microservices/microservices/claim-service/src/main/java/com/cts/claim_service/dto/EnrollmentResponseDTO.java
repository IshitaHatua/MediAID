package com.cts.claim_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollmentResponseDTO {

    private Long enrollmentId;
    private Long citizenId;
    private Long schemeId;
    private String enrollmentDate;
    private String expiryDate;
    private String status;
}
