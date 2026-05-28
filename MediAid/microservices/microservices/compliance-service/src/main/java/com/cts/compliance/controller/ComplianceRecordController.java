package com.cts.compliance.controller;

import com.cts.compliance.api.APIResponse;
import com.cts.compliance.dto.*;
import com.cts.compliance.service.ComplianceRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceRecordController {

    private final ComplianceRecordService service;

    public ComplianceRecordController(ComplianceRecordService service) {
        this.service = service;
    }

    /**
     * Simple evaluate — called by audit-service via Feign.
     * Security: permitAll (declared in SecurityConfig). Unchanged.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<APIResponse<ComplianceEvaluationResponseDTO>> evaluateSimple(
            @RequestParam Long entityId,
            @RequestParam String entityType,
            @RequestParam Long requestedBy) {

        ComplianceEvaluationResponseDTO result = service.evaluateSimple(entityId, entityType, requestedBy);
        return ResponseEntity.ok(APIResponse.<ComplianceEvaluationResponseDTO>builder()
                .status("SUCCESS")
                .message("Compliance evaluation completed")
                .data(result)
                .build());
    }

    /**
     * Full evaluate — COMPLIANCE is the primary actor per the project spec.
     * AUDITOR excluded: read-only, cannot trigger evaluations.
     */
    @PostMapping("/evaluate/full")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE')")
    public ResponseEntity<APIResponse<ComplianceEvaluationResponseDTO>> evaluateFull(
            @Valid @RequestBody ComplianceEvaluationRequestDTO dto) {

        ComplianceEvaluationResponseDTO result = service.evaluateFull(dto);
        return ResponseEntity.ok(APIResponse.<ComplianceEvaluationResponseDTO>builder()
                .status("SUCCESS")
                .message("Full compliance evaluation completed")
                .data(result)
                .build());
    }

    /**
     * Manual check — COMPLIANCE is the primary actor for manual checks.
     * AUDITOR excluded: read-only, cannot create records.
     */
    @PostMapping("/records")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE')")
    public ResponseEntity<APIResponse<ComplianceRecordResponseDTO>> manualCheck(
            @Valid @RequestBody ComplianceRecordRequestDTO dto) {

        ComplianceRecordResponseDTO saved = service.manualCheck(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                APIResponse.<ComplianceRecordResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Compliance record created")
                        .data(saved)
                        .build());
    }

    // COMPLIANCE and AUDITOR added: both roles need full read access to compliance records
    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<ComplianceRecordResponseDTO>>> getAll() {
        return ResponseEntity.ok(APIResponse.<List<ComplianceRecordResponseDTO>>builder()
                .status("SUCCESS")
                .message("Compliance records fetched")
                .data(service.getAll())
                .build());
    }

    @GetMapping("/records/{complianceId}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<ComplianceRecordResponseDTO>> getById(
            @PathVariable Long complianceId) {
        return ResponseEntity.ok(APIResponse.<ComplianceRecordResponseDTO>builder()
                .status("SUCCESS")
                .message("Compliance record fetched")
                .data(service.getById(complianceId))
                .build());
    }

    @GetMapping("/records/entity/{entityId}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<ComplianceRecordResponseDTO>>> getByEntityId(
            @PathVariable Long entityId) {
        return ResponseEntity.ok(APIResponse.<List<ComplianceRecordResponseDTO>>builder()
                .status("SUCCESS")
                .message("Compliance records for entity " + entityId)
                .data(service.getByEntityId(entityId))
                .build());
    }

    @GetMapping("/records/type/{entityType}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<ComplianceRecordResponseDTO>>> getByEntityType(
            @PathVariable String entityType) {
        return ResponseEntity.ok(APIResponse.<List<ComplianceRecordResponseDTO>>builder()
                .status("SUCCESS")
                .message("Compliance records of type " + entityType)
                .data(service.getByEntityType(entityType))
                .build());
    }

    /** Violations dashboard — COMPLIANCE and AUDITOR are the primary reviewers per the project spec. */
    @GetMapping("/records/violations")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<ComplianceRecordResponseDTO>>> getViolations() {
        return ResponseEntity.ok(APIResponse.<List<ComplianceRecordResponseDTO>>builder()
                .status("SUCCESS")
                .message("Compliance violations fetched")
                .data(service.getViolations())
                .build());
    }

    /** Flagged records queue — COMPLIANCE and AUDITOR are the primary reviewers per the project spec. */
    @GetMapping("/records/flagged")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<ComplianceRecordResponseDTO>>> getFlagged() {
        return ResponseEntity.ok(APIResponse.<List<ComplianceRecordResponseDTO>>builder()
                .status("SUCCESS")
                .message("Flagged compliance records fetched")
                .data(service.getFlagged())
                .build());
    }
}
