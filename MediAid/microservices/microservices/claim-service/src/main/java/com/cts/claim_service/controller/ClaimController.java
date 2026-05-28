package com.cts.claim_service.controller;

import com.cts.claim_service.api.APIResponse;
import com.cts.claim_service.dto.ClaimDocumentResponseDTO;
import com.cts.claim_service.dto.ClaimRequestDTO;
import com.cts.claim_service.dto.ClaimResponseDTO;
import com.cts.claim_service.dto.ClaimStatusUpdateDTO;
import com.cts.claim_service.exception.ResourceNotFoundException;
import com.cts.claim_service.model.ClaimValidation;
import com.cts.claim_service.security.CurrentUserUtil;
import com.cts.claim_service.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final CurrentUserUtil currentUserUtil;

    @Value("${file.upload-dir:./claim-uploads}")
    private String uploadDir;

    public ClaimController(ClaimService claimService, CurrentUserUtil currentUserUtil) {
        this.claimService = claimService;
        this.currentUserUtil = currentUserUtil;
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<APIResponse<ClaimResponseDTO>> createClaim(
            @Valid @RequestBody ClaimRequestDTO dto) {
        Long citizenId = currentUserUtil.getUserId();
        ClaimResponseDTO responseDTO = claimService.createClaim(citizenId, dto);
        return ResponseEntity.ok(APIResponse.<ClaimResponseDTO>builder()
                .status("SUCCESS").message("Claim created successfully").data(responseDTO).build());
    }

    @PreAuthorize("hasAnyRole('OFFICER', 'COMPLIANCE')")
    @GetMapping("/{claimId}")
    public ResponseEntity<APIResponse<ClaimResponseDTO>> getClaim(
            @PathVariable Long claimId) throws ResourceNotFoundException {
        ClaimResponseDTO responseDTO = claimService.getClaimById(claimId);
        return ResponseEntity.ok(APIResponse.<ClaimResponseDTO>builder()
                .status("SUCCESS").message("Claim retrieved").data(responseDTO).build());
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping
    public ResponseEntity<APIResponse<List<ClaimResponseDTO>>> getMyClaims() {
        Long citizenId = currentUserUtil.getUserId();
        List<ClaimResponseDTO> dtos = claimService.getClaimsByCitizen(citizenId);
        return ResponseEntity.ok(APIResponse.<List<ClaimResponseDTO>>builder()
                .status("SUCCESS").message("Claims retrieved").data(dtos).build());
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PatchMapping("/{claimId}/status")
    public ResponseEntity<APIResponse<ClaimResponseDTO>> updateClaimStatus(
            @PathVariable Long claimId,
            @Valid @RequestBody ClaimStatusUpdateDTO dto) throws ResourceNotFoundException {
        Long officerId = currentUserUtil.getUserId();
        ClaimResponseDTO responseDTO = claimService.updateClaimStatus(claimId, dto, officerId);
        return ResponseEntity.ok(APIResponse.<ClaimResponseDTO>builder()
                .status("SUCCESS").message("Claim status updated").data(responseDTO).build());
    }

    @PreAuthorize("hasAnyRole('OFFICER', 'COMPLIANCE')")
    @GetMapping("/all")
    public ResponseEntity<APIResponse<List<ClaimResponseDTO>>> getAllClaims() {
        List<ClaimResponseDTO> dtos = claimService.getAllClaims();
        return ResponseEntity.ok(APIResponse.<List<ClaimResponseDTO>>builder()
                .status("SUCCESS").message("All claims retrieved").data(dtos).build());
    }

    @PreAuthorize("hasRole('OFFICER')")
    @GetMapping("/validations")
    public ResponseEntity<APIResponse<List<ClaimValidation>>> getValidations() {
        List<ClaimValidation> validations = claimService.getClaimsValidations();
        return ResponseEntity.ok(APIResponse.<List<ClaimValidation>>builder()
                .status("SUCCESS").message("Claim validations fetched").data(validations).build());
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping(value = "/{claimId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ClaimDocumentResponseDTO>> uploadDocument(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file) {
        Long citizenId = currentUserUtil.getUserId();
        ClaimDocumentResponseDTO responseDTO = claimService.uploadDocument(claimId, citizenId, file);
        return ResponseEntity.ok(APIResponse.<ClaimDocumentResponseDTO>builder()
                .status("SUCCESS").message("Document uploaded successfully").data(responseDTO).build());
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER')")
    @GetMapping("/{claimId}/documents")
    public ResponseEntity<APIResponse<List<ClaimDocumentResponseDTO>>> getDocuments(
            @PathVariable Long claimId) {
        List<ClaimDocumentResponseDTO> documents = claimService.getDocumentsByClaimId(claimId);
        return ResponseEntity.ok(APIResponse.<List<ClaimDocumentResponseDTO>>builder()
                .status("SUCCESS").message("Documents retrieved").data(documents).build());
    }

    /**
     * Internal endpoint — called by compliance-service (and other microservices) via Feign.
     * No @PreAuthorize: inter-service calls do not carry a user JWT.
     * Permitted without auth in SecurityConfig under /api/claims/internal/**.
     */
    @GetMapping("/internal/{claimId}")
    public ResponseEntity<APIResponse<ClaimResponseDTO>> getClaimInternal(
            @PathVariable Long claimId) throws ResourceNotFoundException {
        ClaimResponseDTO responseDTO = claimService.getClaimById(claimId);
        return ResponseEntity.ok(APIResponse.<ClaimResponseDTO>builder()
                .status("SUCCESS").message("Claim retrieved internally").data(responseDTO).build());
    }

    @GetMapping("/{claimId}/citizen")
    public ResponseEntity<APIResponse<Long>> getCitizenIdByClaimId(@PathVariable Long claimId) {
        Long citizenId = claimService.getCitizenIdByClaimId(claimId);
        return ResponseEntity.ok(APIResponse.<Long>builder()
                .status("SUCCESS").message("Citizen ID retrieved").data(citizenId).build());
    }

    @GetMapping("/{claimId}/amount")
    public ResponseEntity<APIResponse<Double>> getClaimAmountByClaimId(@PathVariable Long claimId) {
        Double amount = claimService.getClaimAmountByClaimId(claimId);
        return ResponseEntity.ok(APIResponse.<Double>builder()
                .status("SUCCESS").message("Claim amount retrieved").data(amount).build());
    }

    /**
     * Download a claim document by file name.
     * No @PreAuthorize — permitted without role restrictions in SecurityConfig
     * under /api/claims/documents/** so both citizens and officers can access it.
     */
    @GetMapping("/documents/{fileName}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
    }
}
