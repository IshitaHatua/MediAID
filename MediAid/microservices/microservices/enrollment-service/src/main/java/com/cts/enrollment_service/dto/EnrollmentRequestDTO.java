package com.cts.enrollment_service.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class  EnrollmentRequestDTO {

    @NotNull(message = "Scheme ID is required")
    private Long schemeId;

}
