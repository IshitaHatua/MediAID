package com.cts.enrollment_service.serviceImpl;

import com.cts.enrollment_service.api.APIResponse;
import com.cts.enrollment_service.client.AuditManagementFeignClient;
import com.cts.enrollment_service.client.ComplianceFeignClient;
import com.cts.enrollment_service.client.SchemeClient;
import com.cts.enrollment_service.dto.AuditManagementLogRequest;
import com.cts.enrollment_service.dto.ComplianceEvaluationResponseDTO;
import com.cts.enrollment_service.dto.EnrollmentRequestDTO;
import com.cts.enrollment_service.dto.EnrollmentResponseDTO;
import com.cts.enrollment_service.dto.SchemeResponseDTO;
import com.cts.enrollment_service.exception.BadRequestException;
import com.cts.enrollment_service.exception.ResourceNotFoundException;
import com.cts.enrollment_service.mapper.EnrollmentMapper;
import com.cts.enrollment_service.model.Enrollment;
import com.cts.enrollment_service.repository.EnrollmentRepository;
import com.cts.enrollment_service.service.EnrollmentService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SchemeClient schemeClient;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    // ── New: audit and compliance integration ─────────────────────────────────
    @Autowired
    private AuditManagementFeignClient auditManagementFeignClient;

    @Autowired
    private ComplianceFeignClient complianceFeignClient;

    @Override
    @CircuitBreaker(name = "schemeService", fallbackMethod = "schemeFallback")
    public EnrollmentResponseDTO createEnrollment(Long citizenId, EnrollmentRequestDTO dto) {
        APIResponse<SchemeResponseDTO> response = schemeClient.getSchemeById(dto.getSchemeId());
        SchemeResponseDTO scheme = response.getData();

        if ("INACTIVE".equalsIgnoreCase(scheme.getStatus())) {
            throw new BadRequestException("Cannot enroll: scheme " + dto.getSchemeId() + " is INACTIVE.");
        }

        LocalDate enrollmentDate = LocalDate.now();
        LocalDate expiryDate = enrollmentDate.plusYears(scheme.getValidityYears());

        Enrollment enrollment = enrollmentMapper.toEntity(dto);
        enrollment.setCitizenId(citizenId);
        enrollment.setEnrollmentDate(enrollmentDate.toString());
        enrollment.setExpiryDate(expiryDate.toString());
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);

        EnrollmentResponseDTO saved = enrollmentMapper.toDto(enrollmentRepository.save(enrollment));

        // Log enrollment submission to audit-management-service (best-effort).
        writeAuditLog(citizenId, "ENROLLMENT_SUBMITTED",
                "ENROLLMENT:" + saved.getEnrollmentId(),
                "SchemeId=" + dto.getSchemeId() + ", ExpiryDate=" + expiryDate);

        return saved;
    }

    @Override
    public EnrollmentResponseDTO getEnrollmentById(Long enrollmentId) throws ResourceNotFoundException {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        return enrollmentMapper.toDto(enrollment);
    }

    @Override
    public List<EnrollmentResponseDTO> getEnrollmentsByCitizen(Long citizenId) {
        return enrollmentRepository.findByCitizenId(citizenId)
                .stream()
                .map(enrollmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponseDTO> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(enrollmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnrollmentResponseDTO updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus status)
            throws ResourceNotFoundException {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new BadRequestException("Only pending enrollments can be updated");
        }

        enrollment.setStatus(status);
        Enrollment saved = enrollmentRepository.save(enrollment);
        EnrollmentResponseDTO result = enrollmentMapper.toDto(saved);

        // Log status change to audit-management-service (best-effort).
        writeAuditLog(enrollment.getCitizenId(), "ENROLLMENT_" + status.name(),
                "ENROLLMENT:" + enrollmentId,
                "SchemeId=" + enrollment.getSchemeId() + ", Status changed to " + status.name());

        // Auto-trigger POLICY compliance evaluation when enrollment is APPROVED (best-effort).
        if (status == Enrollment.EnrollmentStatus.APPROVED) {
            triggerComplianceEvaluation(enrollmentId, enrollment.getCitizenId());
        }

        return result;
    }

    @Override
    public Boolean existsEnrollment(Long citizenId, Long schemeId) {
        return enrollmentRepository.existsByCitizenIdAndSchemeIdAndStatus(
                citizenId, schemeId, Enrollment.EnrollmentStatus.APPROVED);
    }

    @Override
    public String getEnrollmentStatus(Long citizenId, Long schemeId) {
        return enrollmentRepository.findByCitizenIdAndSchemeId(citizenId, schemeId)
                .map(enrollment -> enrollment.getStatus().name())
                .orElse("NOT_ENROLLED");
    }

    @Override
    public EnrollmentResponseDTO getEnrollmentByCitizenAndScheme(Long citizenId, Long schemeId) {
        Enrollment enrollment = enrollmentRepository.findByCitizenIdAndSchemeId(citizenId, schemeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for citizen " + citizenId + " and scheme " + schemeId));
        return enrollmentMapper.toDto(enrollment);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void writeAuditLog(Long userId, String action, String resource, String details) {
        try {
            auditManagementFeignClient.log(AuditManagementLogRequest.builder()
                    .userId(userId)
                    .action(action)
                    .resource(resource)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            log.error("[EnrollmentService] Unexpected error writing to audit-management-service: {}", ex.getMessage());
        }
    }

    private void triggerComplianceEvaluation(Long enrollmentId, Long citizenId) {
        try {
            ComplianceEvaluationResponseDTO result =
                    complianceFeignClient.evaluate(enrollmentId, "POLICY", citizenId);
            log.info("[EnrollmentService] Compliance evaluation triggered for POLICY (ENROLLMENT:{}) — Result: {}",
                    enrollmentId, result != null ? result.getResult() : "null");
        } catch (Exception ex) {
            log.error("[EnrollmentService] Unexpected error triggering compliance for ENROLLMENT:{} — {}",
                    enrollmentId, ex.getMessage());
        }
    }

    public EnrollmentResponseDTO schemeFallback(Long citizenId, EnrollmentRequestDTO dto, Throwable t) {
        if (t instanceof ResourceNotFoundException rne) throw rne;
        if (t instanceof BadRequestException bre) throw bre;
        if (t instanceof FeignException fe) {
            if (fe.status() == 404)
                throw new ResourceNotFoundException("Scheme not found with id: " + dto.getSchemeId());
            if (fe.status() == 400)
                throw new BadRequestException("Bad request to Scheme Service: " + fe.getMessage());
        }
        throw new RuntimeException("Scheme Service is currently unavailable. Please try again later.");
    }
}
