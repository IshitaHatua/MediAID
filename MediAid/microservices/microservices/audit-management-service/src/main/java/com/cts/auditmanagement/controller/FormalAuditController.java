package com.cts.auditmanagement.controller;

import com.cts.auditmanagement.api.APIResponse;
import com.cts.auditmanagement.dto.FormalAuditRequestDTO;
import com.cts.auditmanagement.dto.FormalAuditResponseDTO;
import com.cts.auditmanagement.dto.FormalAuditUpdateDTO;
import com.cts.auditmanagement.model.FormalAudit.FormalAuditStatus;
import com.cts.auditmanagement.service.FormalAuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-management/audits")
public class FormalAuditController {

    private final FormalAuditService auditService;

    public FormalAuditController(FormalAuditService auditService) {
        this.auditService = auditService;
    }

    // COMPLIANCE added: primary actor for creating formal audits per project spec
    // AUDITOR excluded: read-only role, cannot create
    @PostMapping
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE')")
    public ResponseEntity<APIResponse<FormalAuditResponseDTO>> createAudit(
            @Valid @RequestBody FormalAuditRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                APIResponse.<FormalAuditResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Formal audit created")
                        .data(auditService.createAudit(dto))
                        .build());
    }

    // COMPLIANCE and AUDITOR added: both roles read all audits
    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<FormalAuditResponseDTO>>> getAllAudits() {
        return ResponseEntity.ok(APIResponse.<List<FormalAuditResponseDTO>>builder()
                .status("SUCCESS")
                .message("Formal audits fetched")
                .data(auditService.getAllAudits())
                .build());
    }

    @GetMapping("/{auditId}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<FormalAuditResponseDTO>> getById(@PathVariable Long auditId) {
        return ResponseEntity.ok(APIResponse.<FormalAuditResponseDTO>builder()
                .status("SUCCESS")
                .message("Formal audit fetched")
                .data(auditService.getAuditById(auditId))
                .build());
    }

    @GetMapping("/officer/{officerId}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<FormalAuditResponseDTO>>> getByOfficer(
            @PathVariable Long officerId) {
        return ResponseEntity.ok(APIResponse.<List<FormalAuditResponseDTO>>builder()
                .status("SUCCESS")
                .message("Audits for officer " + officerId)
                .data(auditService.getAuditsByOfficer(officerId))
                .build());
    }

    @GetMapping("/scope/{scope}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<FormalAuditResponseDTO>>> getByScope(
            @PathVariable String scope) {
        return ResponseEntity.ok(APIResponse.<List<FormalAuditResponseDTO>>builder()
                .status("SUCCESS")
                .message("Audits with scope " + scope)
                .data(auditService.getAuditsByScope(scope))
                .build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<FormalAuditResponseDTO>>> getByStatus(
            @PathVariable FormalAuditStatus status) {
        return ResponseEntity.ok(APIResponse.<List<FormalAuditResponseDTO>>builder()
                .status("SUCCESS")
                .message("Audits with status " + status)
                .data(auditService.getAuditsByStatus(status))
                .build());
    }

    // COMPLIANCE added: can update findings and status
    // AUDITOR excluded: read-only, cannot update
    @PatchMapping("/{auditId}")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE')")
    public ResponseEntity<APIResponse<FormalAuditResponseDTO>> updateAudit(
            @PathVariable Long auditId,
            @Valid @RequestBody FormalAuditUpdateDTO dto) {
        return ResponseEntity.ok(APIResponse.<FormalAuditResponseDTO>builder()
                .status("SUCCESS")
                .message("Formal audit updated")
                .data(auditService.updateAudit(auditId, dto))
                .build());
    }

    /**
     * Triggers compliance evaluation via Feign to compliance-service.
     * Result is written back into FormalAudit.findings and status is updated.
     * COMPLIANCE added: primary trigger actor per project spec
     * AUDITOR excluded: read-only, cannot trigger
     */
    @PostMapping("/{auditId}/trigger-compliance")
    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN','COMPLIANCE')")
    public ResponseEntity<APIResponse<FormalAuditResponseDTO>> triggerCompliance(
            @PathVariable Long auditId) {
        return ResponseEntity.ok(APIResponse.<FormalAuditResponseDTO>builder()
                .status("SUCCESS")
                .message("Compliance evaluation triggered")
                .data(auditService.triggerComplianceEvaluation(auditId))
                .build());
    }
}
