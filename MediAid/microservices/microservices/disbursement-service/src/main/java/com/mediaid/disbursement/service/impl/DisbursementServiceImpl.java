package com.mediaid.disbursement.service.impl;

import com.mediaid.disbursement.client.AuditManagementFeignClient;
import com.mediaid.disbursement.client.ClaimClient;
import com.mediaid.disbursement.client.ComplianceFeignClient;
import com.mediaid.disbursement.dto.AuditManagementLogRequest;
import com.mediaid.disbursement.dto.ComplianceEvaluationResponseDTO;
import com.mediaid.disbursement.dto.request.DisbursementRequestDTO;
import com.mediaid.disbursement.dto.response.ClaimClientResponseDTO;
import com.mediaid.disbursement.dto.response.DisbursementResponseDTO;
import com.mediaid.disbursement.exception.BadRequestException;
import com.mediaid.disbursement.exception.ResourceNotFoundException;
import com.mediaid.disbursement.mapper.DisbursementMapper;
import com.mediaid.disbursement.model.Disbursement;
import com.mediaid.disbursement.repository.DisbursementRepository;
import com.mediaid.disbursement.service.DisbursementService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private final DisbursementRepository repository;
    private final DisbursementMapper mapper;
    private final ClaimClient claimClient;

    // ── New: audit and compliance integration ─────────────────────────────────
    private final AuditManagementFeignClient auditManagementFeignClient;
    private final ComplianceFeignClient complianceFeignClient;

    @Override
    @CircuitBreaker(name = "claim-service", fallbackMethod = "claimFallback")
    public DisbursementResponseDTO createDisbursement(DisbursementRequestDTO requestDTO) {
        Long claimId = requestDTO.getClaimId();
        ClaimClientResponseDTO claimResponse = claimClient.getClaim(claimId);

        if (claimResponse == null || claimResponse.getData() == null) {
            throw new ResourceNotFoundException("Claim not found with id: " + claimId);
        }

        ClaimClientResponseDTO.ClaimData claimData = claimResponse.getData();

        if (!"APPROVED".equalsIgnoreCase(claimData.getStatus())) {
            throw new BadRequestException(
                    "Disbursement can only be created for APPROVED claims. Claim " + claimId
                    + " has status: " + claimData.getStatus());
        }

        Disbursement d = mapper.toEntity(requestDTO);
        d.setClaimId(claimId);
        d.setCitizenId(claimData.getCitizenId());
        d.setSchemeId(claimData.getSchemeId());
        d.setStatus("PROCESSING");

        DisbursementResponseDTO saved = mapper.toDto(repository.save(d));

        // Log disbursement creation to audit-management-service (best-effort).
        writeAuditLog(claimData.getCitizenId(), "DISBURSEMENT_CREATED",
                "DISBURSEMENT:" + saved.getDisbursementId(),
                "ClaimId=" + claimId + ", Amount=" + requestDTO.getAmount()
                        + ", CitizenId=" + claimData.getCitizenId());

        // Auto-trigger compliance evaluation for this disbursement (best-effort).
        triggerComplianceEvaluation(saved.getDisbursementId(), claimData.getCitizenId());

        return saved;
    }

    @Override
    public DisbursementResponseDTO getByClaimId(Long claimId) {
        Disbursement disbursement = repository.findByClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found with claim id: " + claimId));
        return mapper.toDto(disbursement);
    }

    @Override
    public DisbursementResponseDTO getByDisbursementId(Long disbursementId) {
        Disbursement disbursement = repository.findById(disbursementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found with id: " + disbursementId));
        return mapper.toDto(disbursement);
    }

    @Override
    public List<DisbursementResponseDTO> getMyDisbursements(Long citizenId) {
        return repository.findByCitizenId(citizenId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<DisbursementResponseDTO> getAllDisbursements() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public DisbursementResponseDTO updateDisbursementStatus(Long disbursementId, String status) {
        Disbursement disbursement = repository.findById(disbursementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found with id: " + disbursementId));
        disbursement.setStatus(status);
        DisbursementResponseDTO updated = mapper.toDto(repository.save(disbursement));

        // Log status change to audit-management-service (best-effort).
        writeAuditLog(updated.getCitizenId(), "DISBURSEMENT_STATUS_UPDATED",
                "DISBURSEMENT:" + disbursementId,
                "Status changed to " + status);

        return updated;
    }

    @Override
    public BigDecimal getTotalUtilized(List<Long> claimIds) {
        if (claimIds == null || claimIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = repository.sumCompletedByClaimIds(claimIds);
        return total != null ? total : BigDecimal.ZERO;
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
            log.error("[DisbursementService] Unexpected error writing to audit-management-service: {}", ex.getMessage());
        }
    }

    /**
     * Triggers automatic compliance evaluation for a newly created disbursement.
     * Best-effort — fallback returns FLAGGED so the disbursement remains PROCESSING
     * and manual review via audit-management-service is still possible.
     */
    private void triggerComplianceEvaluation(Long disbursementId, Long requestedBy) {
        try {
            ComplianceEvaluationResponseDTO result =
                    complianceFeignClient.evaluate(disbursementId, "DISBURSEMENT", requestedBy);
            log.info("[DisbursementService] Compliance evaluation triggered for DISBURSEMENT:{} — Result: {}",
                    disbursementId, result != null ? result.getResult() : "null");
        } catch (Exception ex) {
            log.error("[DisbursementService] Unexpected error triggering compliance for DISBURSEMENT:{} — {}",
                    disbursementId, ex.getMessage());
        }
    }

    public DisbursementResponseDTO claimFallback(DisbursementRequestDTO requestDTO, Throwable t) {
        log.error("[DisbursementService] claimFallback fired for claimId={} cause={} message={}",
                requestDTO != null ? requestDTO.getClaimId() : "null",
                t.getClass().getSimpleName(), t.getMessage());
        if (t instanceof ResourceNotFoundException rne) throw rne;
        if (t instanceof BadRequestException bre) throw bre;
        if (t instanceof FeignException fe) {
            if (fe.status() == 404)
                throw new ResourceNotFoundException("Claim not found with id: " + requestDTO.getClaimId());
            if (fe.status() == 400)
                throw new BadRequestException("Bad request to Claim Service: " + fe.getMessage());
            if (fe.status() == 401 || fe.status() == 403)
                throw new RuntimeException("Disbursement service is not authorised to contact Claim Service. "
                        + "Check that the API gateway is injecting X-User-Id / X-User-Role headers.");
        }
        throw new RuntimeException("Claim Service is currently unavailable. Please try again later.");
    }
}
