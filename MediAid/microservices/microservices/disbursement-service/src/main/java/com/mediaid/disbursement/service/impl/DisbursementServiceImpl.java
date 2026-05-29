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
        // Honor the requested status (auto-create from claim-service sends "Pending";
        // the older manual UI sent "Pending" too). Normalise to upper case to keep
        // downstream comparisons consistent — sumCompletedByClaimIds and
        // status-badge both expect upper-case tokens.
        String requestedStatus = requestDTO.getStatus() != null
                ? requestDTO.getStatus().toUpperCase()
                : "PENDING";
        d.setStatus(requestedStatus);

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
        // Normalise to upper case so PENDING/PROCESSING/COMPLETED/FAILED are
        // stored consistently regardless of caller casing.
        disbursement.setStatus(status != null ? status.toUpperCase() : null);
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

}
