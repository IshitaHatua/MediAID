package com.cts.enrollment_service.dto;

import lombok.Getter;
import lombok.Setter;
import com.cts.enrollment_service.model.Enrollment.EnrollmentStatus;

@Getter
@Setter
public class EnrollmentResponseDTO {

    private Long enrollmentId;
    private Long citizenId;
    private Long schemeId;
    private String enrollmentDate;
    private String expiryDate;
    private EnrollmentStatus status;
}
