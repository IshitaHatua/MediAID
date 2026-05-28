package com.cts.enrollment_service.mapper;


import org.springframework.stereotype.Component;
import com.cts.enrollment_service.dto.EnrollmentRequestDTO;
import com.cts.enrollment_service.dto.EnrollmentResponseDTO;
import com.cts.enrollment_service.model.Enrollment;

@Component
public class EnrollmentMapper {

    public Enrollment toEntity(EnrollmentRequestDTO dto) {
        Enrollment enrollment = new Enrollment();
        enrollment.setSchemeId(dto.getSchemeId());
        return enrollment;
    }

    public EnrollmentResponseDTO toDto(Enrollment enrollment) {
        EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
        dto.setEnrollmentId(enrollment.getEnrollmentId());
        dto.setCitizenId(enrollment.getCitizenId());
        dto.setSchemeId(enrollment.getSchemeId());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setExpiryDate(enrollment.getExpiryDate());
        dto.setStatus(enrollment.getStatus());
        return dto;
    }
}
