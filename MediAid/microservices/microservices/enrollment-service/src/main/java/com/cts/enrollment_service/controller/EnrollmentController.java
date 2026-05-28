package com.cts.enrollment_service.controller;

import com.cts.enrollment_service.api.APIResponse;
import com.cts.enrollment_service.dto.EnrollmentRequestDTO;
import com.cts.enrollment_service.dto.EnrollmentResponseDTO;
import com.cts.enrollment_service.model.Enrollment;
import com.cts.enrollment_service.security.CurrentUserUtil;
import com.cts.enrollment_service.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CurrentUserUtil currentUserUtil;

    public EnrollmentController(EnrollmentService enrollmentService, CurrentUserUtil currentUserUtil) {
        this.enrollmentService = enrollmentService;
        this.currentUserUtil = currentUserUtil;
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<APIResponse<EnrollmentResponseDTO>> createEnrollment(
            @Valid @RequestBody EnrollmentRequestDTO dto) {

        Long citizenId = currentUserUtil.getUserId();
        EnrollmentResponseDTO responseDTO = enrollmentService.createEnrollment(citizenId, dto);

        return ResponseEntity.ok(APIResponse.<EnrollmentResponseDTO>builder()
                .status("SUCCESS")
                .message("Enrollment created successfully")
                .data(responseDTO)
                .build());
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER', 'ADMIN', 'COMPLIANCE')")
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<EnrollmentResponseDTO>> getEnrollment(@PathVariable Long id) {

        EnrollmentResponseDTO dto = enrollmentService.getEnrollmentById(id);

        return ResponseEntity.ok(APIResponse.<EnrollmentResponseDTO>builder()
                .status("SUCCESS")
                .message("Enrollment retrieved")
                .data(dto)
                .build());
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping
    public ResponseEntity<APIResponse<List<EnrollmentResponseDTO>>> getMyEnrollments() {

        Long citizenId = currentUserUtil.getUserId();
        List<EnrollmentResponseDTO> dtos = enrollmentService.getEnrollmentsByCitizen(citizenId);

        return ResponseEntity.ok(APIResponse.<List<EnrollmentResponseDTO>>builder()
                .status("SUCCESS")
                .message("Enrollments retrieved")
                .data(dtos)
                .build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'COMPLIANCE')")
    @GetMapping("/all")
    public ResponseEntity<APIResponse<List<EnrollmentResponseDTO>>> getAllEnrollments() {

        List<EnrollmentResponseDTO> dtos = enrollmentService.getAllEnrollments();

        return ResponseEntity.ok(APIResponse.<List<EnrollmentResponseDTO>>builder()
                .status("SUCCESS")
                .message("All enrollments retrieved")
                .data(dtos)
                .build());
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<APIResponse<EnrollmentResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam Enrollment.EnrollmentStatus status) {

        EnrollmentResponseDTO responseDTO = enrollmentService.updateEnrollmentStatus(id, status);

        return ResponseEntity.ok(APIResponse.<EnrollmentResponseDTO>builder()
                .status("SUCCESS")
                .message("Status updated")
                .data(responseDTO)
                .build());
    }

    // Internal service-to-service endpoints — accept explicit citizenId/schemeId params,
    // covered by the global authenticated() rule in SecurityConfig.
    @GetMapping("/validate")
    public Boolean validateEnrollment(@RequestParam Long citizenId, @RequestParam Long schemeId) {
        return enrollmentService.existsEnrollment(citizenId, schemeId);
    }

    @GetMapping("/enrollment-status")
    public String getEnrollmentStatus(@RequestParam Long citizenId, @RequestParam Long schemeId) {
        return enrollmentService.getEnrollmentStatus(citizenId, schemeId);
    }

    @GetMapping("/details")
    public ResponseEntity<APIResponse<EnrollmentResponseDTO>> getEnrollmentDetails(
            @RequestParam Long citizenId, @RequestParam Long schemeId) {
        EnrollmentResponseDTO dto = enrollmentService.getEnrollmentByCitizenAndScheme(citizenId, schemeId);
        return ResponseEntity.ok(APIResponse.<EnrollmentResponseDTO>builder()
                .status("SUCCESS")
                .message("Enrollment details retrieved")
                .data(dto)
                .build());
    }
}
