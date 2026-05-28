package com.cts.claim_service.serviceImpl;

import com.cts.claim_service.api.APIResponse;
import com.cts.claim_service.client.AuditManagementFeignClient;
import com.cts.claim_service.client.ComplianceFeignClient;
import com.cts.claim_service.client.DisbursementClient;
import com.cts.claim_service.client.EnrollmentClient;
import com.cts.claim_service.client.SchemeClient;
import com.cts.claim_service.dto.*;
import com.cts.claim_service.mapper.ClaimMapper;
import com.cts.claim_service.model.Claim;
import com.cts.claim_service.model.ClaimDocument;
import com.cts.claim_service.model.ClaimValidation;
import com.cts.claim_service.repository.ClaimDocumentRepository;
import com.cts.claim_service.repository.ClaimRepository;
import com.cts.claim_service.repository.ClaimValidationRepository;
import com.cts.claim_service.service.ClaimService;
import com.cts.claim_service.exception.BadRequestException;
import com.cts.claim_service.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimServiceImpl.class);

    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private EnrollmentClient enrollmentClient;
    @Autowired
    private SchemeClient schemeClient;
    @Autowired
    private DisbursementClient disbursementClient;
    @Autowired
    private ClaimValidationRepository validationRepository;
    @Autowired
    private ClaimDocumentRepository claimDocumentRepository;
    @Autowired
    private ClaimMapper claimMapper;

    // ── New: audit and compliance integration ─────────────────────────────────
    @Autowired
    private AuditManagementFeignClient auditManagementFeignClient;
    @Autowired
    private ComplianceFeignClient complianceFeignClient;

    @Value("${app.upload.dir:uploads/claim-documents}")
    private String uploadDirectory;

    @Override
    @CircuitBreaker(name = "claimExternal", fallbackMethod = "claimFallback")
    public ClaimResponseDTO createClaim(Long citizenId, ClaimRequestDTO dto) {
        // Step 1: Validate scheme.
        // FeignErrorDecoder converts 404 → ResourceNotFoundException (bypasses circuit breaker via ignoreExceptions).
        APIResponse<SchemeResponseDTO> schemeResponse = schemeClient.getSchemeById(dto.getSchemeId());
        SchemeResponseDTO scheme = schemeResponse.getData();

        if ("INACTIVE".equalsIgnoreCase(scheme.getStatus())) {
            throw new BadRequestException("Cannot raise a claim: scheme " + dto.getSchemeId() + " is INACTIVE.");
        }
        if (dto.getClaimAmount() > scheme.getMaxCoverageAmount()) {
            throw new BadRequestException("Claim amount exceeds scheme maximum coverage of "
                    + scheme.getMaxCoverageAmount() + ".");
        }

        // Step 2: Validate enrollment status and expiry.
        String enrollmentStatus = enrollmentClient.getEnrollmentStatus(citizenId, dto.getSchemeId());
        if ("NOT_ENROLLED".equals(enrollmentStatus)) {
            throw new BadRequestException("Citizen is not enrolled in this scheme.");
        }
        if (!"APPROVED".equals(enrollmentStatus)) {
            throw new BadRequestException("Enrollment is not approved. Current status: " + enrollmentStatus + ".");
        }

        APIResponse<EnrollmentResponseDTO> enrollmentResponse =
                enrollmentClient.getEnrollmentDetails(citizenId, dto.getSchemeId());
        EnrollmentResponseDTO enrollment = enrollmentResponse.getData();

        LocalDate today = LocalDate.now();
        LocalDate expiryDate = LocalDate.parse(enrollment.getExpiryDate());
        if (today.isAfter(expiryDate)) {
            throw new BadRequestException("Enrollment has expired on " + expiryDate + ".");
        }

        // Step 3: Validate remaining scheme coverage.
        // Utilized = sum of COMPLETED disbursements for all APPROVED claims on this scheme by this citizen.
        List<Claim> approvedClaims = claimRepository.findByCitizenIdAndSchemeIdAndStatus(
                citizenId, dto.getSchemeId(), Claim.ClaimStatus.APPROVED);

        double remainingBalance = scheme.getMaxCoverageAmount();
        if (!approvedClaims.isEmpty()) {
            List<Long> approvedClaimIds = approvedClaims.stream()
                    .map(Claim::getClaimId)
                    .collect(Collectors.toList());
            BigDecimal utilized = disbursementClient.getTotalUtilized(approvedClaimIds);
            remainingBalance = scheme.getMaxCoverageAmount()
                    - (utilized != null ? utilized.doubleValue() : 0.0);
        }

        if (remainingBalance <= 0) {
            throw new BadRequestException("Scheme coverage amount exhausted for scheme " + dto.getSchemeId() + ".");
        }
        if (dto.getClaimAmount() > remainingBalance) {
            throw new BadRequestException(
                    "Claim amount exceeds remaining coverage. Remaining amount: " + remainingBalance + ".");
        }

        // Step 4: Persist claim with PENDING status.
        Claim claim = claimMapper.toEntity(dto);
        claim.setCitizenId(citizenId);
        claim.setClaimDate(today.toString());
        claim.setStatus(Claim.ClaimStatus.PENDING);

        Claim saved = claimRepository.save(claim);

        // Step 5: Log claim submission to audit-management-service (best-effort, fallback handles failure).
        writeAuditLog(citizenId, "CLAIM_SUBMITTED",
                "CLAIM:" + saved.getClaimId(),
                "Amount=" + dto.getClaimAmount() + ", SchemeId=" + dto.getSchemeId());

        return claimMapper.toDto(saved);
    }

    @Override
    public ClaimResponseDTO getClaimById(Long claimId) throws ResourceNotFoundException {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
        return claimMapper.toDto(claim);
    }

    @Override
    public List<ClaimResponseDTO> getClaimsByCitizen(Long citizenId) {
        return claimRepository.findByCitizenId(citizenId)
                .stream()
                .map(claimMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClaimResponseDTO> getAllClaims() {
        return claimRepository.findAll()
                .stream()
                .map(claimMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClaimResponseDTO updateClaimStatus(Long claimId, ClaimStatusUpdateDTO dto, Long officerId)
            throws ResourceNotFoundException {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        if (claim.getStatus() != Claim.ClaimStatus.PENDING) {
            throw new BadRequestException("Only pending claims can be updated.");
        }

        claim.setStatus(dto.getStatus());
        Claim updated = claimRepository.save(claim);

        ClaimValidation validation = new ClaimValidation();
        validation.setClaimId(updated.getClaimId());
        validation.setOfficerId(officerId);
        validation.setValidationDate(LocalDate.now().toString());
        validation.setResult(dto.getStatus().name());
        validation.setRemarks(dto.getRemarks());

        validationRepository.save(validation);

        // Log status change to audit-management-service (best-effort).
        writeAuditLog(officerId, "CLAIM_" + dto.getStatus().name(),
                "CLAIM:" + claimId,
                "Remarks=" + (dto.getRemarks() != null ? dto.getRemarks() : "none"));

        // Auto-trigger compliance evaluation when a claim is APPROVED.
        // Best-effort — claim stays APPROVED even if compliance-service is unavailable.
        if (dto.getStatus() == Claim.ClaimStatus.APPROVED) {
            triggerComplianceEvaluation(claimId, officerId);
        }

        return claimMapper.toDto(updated);
    }

    @Override
    public List<ClaimValidation> getClaimsValidations() {
        return validationRepository.findAll();
    }

    @Override
    public ClaimDocumentResponseDTO uploadDocument(Long claimId, Long citizenId, MultipartFile file) {
        claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        Path dirPath = Paths.get(uploadDirectory, String.valueOf(claimId));
        try {
            Files.createDirectories(dirPath);

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = dirPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), filePath);

            ClaimDocument document = new ClaimDocument();
            document.setClaimId(claimId);
            document.setFileName(originalFilename);
            document.setFilePath(filePath.toAbsolutePath().toString());
            document.setUploadDate(LocalDate.now().toString());
            document.setUploadedBy(citizenId);

            ClaimDocument saved = claimDocumentRepository.save(document);
            return toDocumentDto(saved);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
    }

    @Override
    public List<ClaimDocumentResponseDTO> getDocumentsByClaimId(Long claimId) {
        claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
        return claimDocumentRepository.findByClaimId(claimId)
                .stream()
                .map(this::toDocumentDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getCitizenIdByClaimId(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId))
                .getCitizenId();
    }

    @Override
    public Double getClaimAmountByClaimId(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId))
                .getClaimAmount();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Writes an activity log entry to audit-management-service.
     * Fire-and-forget — never blocks or rolls back the caller's transaction.
     */
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
            log.error("[ClaimService] Unexpected error writing to audit-management-service: {}", ex.getMessage());
        }
    }

    /**
     * Triggers automatic compliance evaluation for an approved claim.
     * Called only when status is set to APPROVED. Best-effort — fallback
     * returns FLAGGED so the claim remains APPROVED and manual review is possible.
     */
    private void triggerComplianceEvaluation(Long claimId, Long officerId) {
        try {
            ComplianceEvaluationResponseDTO result =
                    complianceFeignClient.evaluate(claimId, "CLAIM", officerId);
            log.info("[ClaimService] Compliance evaluation triggered for CLAIM:{} — Result: {}",
                    claimId, result != null ? result.getResult() : "null");
        } catch (Exception ex) {
            log.error("[ClaimService] Unexpected error triggering compliance for CLAIM:{} — {}",
                    claimId, ex.getMessage());
        }
    }

    private ClaimDocumentResponseDTO toDocumentDto(ClaimDocument doc) {
        ClaimDocumentResponseDTO dto = new ClaimDocumentResponseDTO();
        dto.setDocumentId(doc.getDocumentId());
        dto.setClaimId(doc.getClaimId());
        dto.setFileName(doc.getFileName());
        dto.setFilePath(doc.getFilePath());
        dto.setUploadDate(doc.getUploadDate());
        dto.setUploadedBy(doc.getUploadedBy());
        return dto;
    }

    // Primary guard: ignoreExceptions config bypasses this fallback for ResourceNotFoundException
    // and BadRequestException. This fallback handles genuine service outages AND acts as a safety
    // net when FeignException reaches here instead of the converted exception.
    public ClaimResponseDTO claimFallback(Long citizenId, ClaimRequestDTO dto, Throwable t) {
        if (t instanceof ResourceNotFoundException rne) {
            throw rne;
        }
        if (t instanceof BadRequestException bre) {
            throw bre;
        }
        if (t instanceof FeignException fe) {
            if (fe.status() == 404) {
                throw new ResourceNotFoundException("Resource not found: " + fe.getMessage());
            }
            if (fe.status() == 400) {
                throw new BadRequestException("Bad request: " + fe.getMessage());
            }
        }
        throw new RuntimeException("An external service is currently unavailable. Please try again later.");
    }
}
