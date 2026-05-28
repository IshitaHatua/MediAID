//package com.cts.enrollment_service.service;
//
////import com.cts.dto.EnrollmentRequest;
//import com.cts.enrollment_service.exception.ResourceNotFoundException;
//import com.cts.enrollment_service.model.Enrollment;
//
//
//import java.util.List;
//
//public interface EnrollmentService {
//
//    Enrollment createEnrollment(Long citizenId, Enrollment enrollment);
//    Enrollment getEnrollmentById(Long enrollmentId) throws ResourceNotFoundException;
//    List<Enrollment> getEnrollmentsByCitizen(Long citizenId);
//    Enrollment updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus status) throws ResourceNotFoundException;
//    Boolean existsEnrollment(Long citizenId,Long schemeId);
//}
package com.cts.enrollment_service.service;

import com.cts.enrollment_service.dto.EnrollmentRequestDTO;
import com.cts.enrollment_service.dto.EnrollmentResponseDTO;
import com.cts.enrollment_service.model.Enrollment;
import com.cts.enrollment_service.exception.ResourceNotFoundException;

import java.util.List;

public interface EnrollmentService {
    EnrollmentResponseDTO createEnrollment(Long citizenId, EnrollmentRequestDTO dto);
    EnrollmentResponseDTO getEnrollmentById(Long enrollmentId) throws ResourceNotFoundException;
    List<EnrollmentResponseDTO> getEnrollmentsByCitizen(Long citizenId);
    List<EnrollmentResponseDTO> getAllEnrollments();
    EnrollmentResponseDTO updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus status) throws ResourceNotFoundException;
    Boolean existsEnrollment(Long citizenId, Long schemeId);

    String getEnrollmentStatus(Long citizenId, Long schemeId);

    EnrollmentResponseDTO getEnrollmentByCitizenAndScheme(Long citizenId, Long schemeId);
}