package com.mediaid.disbursement.controller;

import com.mediaid.disbursement.api.APIResponse;
import com.mediaid.disbursement.dto.request.DisbursementRequestDTO;
import com.mediaid.disbursement.dto.response.DisbursementResponseDTO;
import com.mediaid.disbursement.security.CurrentUserUtil;
import com.mediaid.disbursement.service.DisbursementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/disbursement")
@RequiredArgsConstructor
public class DisbursementController {

    private final DisbursementService disbursementService;
    private final CurrentUserUtil currentUserUtil;

    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping
    public ResponseEntity<APIResponse<DisbursementResponseDTO>> createDisbursement(
            @RequestBody @Valid DisbursementRequestDTO requestDTO) {
        DisbursementResponseDTO disbursement = disbursementService.createDisbursement(requestDTO);
        return ResponseEntity.ok(APIResponse.<DisbursementResponseDTO>builder()
                .status("SUCCESS").message("Created Disbursement").data(disbursement).build());
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER', 'COMPLIANCE')")
    @GetMapping("/{disbursementId}")
    public ResponseEntity<APIResponse<DisbursementResponseDTO>> getDisbursementById(
            @PathVariable Long disbursementId) {
        DisbursementResponseDTO disbursement = disbursementService.getByDisbursementId(disbursementId);
        return ResponseEntity.ok(APIResponse.<DisbursementResponseDTO>builder()
                .status("SUCCESS").message("Fetched Disbursement").data(disbursement).build());
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER')")
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<APIResponse<DisbursementResponseDTO>> getDisbursementByClaimId(
            @PathVariable Long claimId) {
        DisbursementResponseDTO disbursement = disbursementService.getByClaimId(claimId);
        return ResponseEntity.ok(APIResponse.<DisbursementResponseDTO>builder()
                .status("SUCCESS").message("Fetched Disbursement").data(disbursement).build());
    }

    // CITIZEN views their own disbursements — citizenId sourced from JWT, never hardcoded.
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<DisbursementResponseDTO>>> getMyDisbursements() {
        Long citizenId = currentUserUtil.getUserId();
        List<DisbursementResponseDTO> disbursements = disbursementService.getMyDisbursements(citizenId);
        return ResponseEntity.ok(APIResponse.<List<DisbursementResponseDTO>>builder()
                .status("SUCCESS").message("Fetched My Disbursements").data(disbursements).build());
    }

    @PreAuthorize("hasAnyRole('OFFICER', 'COMPLIANCE')")
    @GetMapping("/all")
    public ResponseEntity<APIResponse<List<DisbursementResponseDTO>>> getAllDisbursements() {
        List<DisbursementResponseDTO> disbursements = disbursementService.getAllDisbursements();
        return ResponseEntity.ok(APIResponse.<List<DisbursementResponseDTO>>builder()
                .status("SUCCESS").message("Fetched All Disbursements").data(disbursements).build());
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PatchMapping("/{disbursementId}/status")
    public ResponseEntity<APIResponse<DisbursementResponseDTO>> updateDisbursementStatus(
            @PathVariable Long disbursementId,
            @RequestParam String status) {
        DisbursementResponseDTO disbursement = disbursementService.updateDisbursementStatus(disbursementId, status);
        return ResponseEntity.ok(APIResponse.<DisbursementResponseDTO>builder()
                .status("SUCCESS").message("Updated Disbursement Status to " + status).data(disbursement).build());
    }

    // Internal inter-service endpoint — called by claim-service to compute utilized coverage.
    // No role restriction; covered by the global authenticated() rule in SecurityConfig.
    @GetMapping("/utilized")
    public BigDecimal getTotalUtilized(@RequestParam List<Long> claimIds) {
        return disbursementService.getTotalUtilized(claimIds);
    }
}
