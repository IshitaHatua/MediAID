package com.cts.auditmanagement.serviceImpl;

import com.cts.auditmanagement.client.ComplianceFeignClient;
import com.cts.auditmanagement.dto.*;
import com.cts.auditmanagement.exception.BadRequestException;
import com.cts.auditmanagement.exception.ResourceNotFoundException;
import com.cts.auditmanagement.mapper.FormalAuditMapper;
import com.cts.auditmanagement.model.FormalAudit;
import com.cts.auditmanagement.model.FormalAudit.FormalAuditStatus;
import com.cts.auditmanagement.repository.FormalAuditRepository;
import com.cts.auditmanagement.service.AuditManagementLogService;
import com.cts.auditmanagement.service.FormalAuditService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FormalAuditServiceImpl implements FormalAuditService {

    private final FormalAuditRepository auditRepository;
    private final AuditManagementLogService logService;
    private final FormalAuditMapper mapper;
    private final ComplianceFeignClient complianceFeignClient;

    public FormalAuditServiceImpl(FormalAuditRepository auditRepository,
                                   AuditManagementLogService logService,
                                   FormalAuditMapper mapper,
                                   ComplianceFeignClient complianceFeignClient) {
        this.auditRepository       = auditRepository;
        this.logService             = logService;
        this.mapper                 = mapper;
        this.complianceFeignClient = complianceFeignClient;
    }

    @Override
    @Transactional
    public FormalAuditResponseDTO createAudit(FormalAuditRequestDTO dto) {
        FormalAudit audit = mapper.toEntity(dto);
        FormalAudit saved = auditRepository.save(audit);

        logService.createLog(AuditManagementLogRequest.builder()
                .userId(dto.getOfficerId())
                .action("FORMAL_AUDIT_CREATED")
                .resource(saved.getScope() + ":" + saved.getScopeEntityId())
                .details("FormalAudit id=" + saved.getAuditId() + " created with status PLANNED")
                .timestamp(LocalDateTime.now())
                .build());

        return mapper.toDto(saved);
    }

    @Override
    public FormalAuditResponseDTO getAuditById(Long auditId) {
        return mapper.toDto(findById(auditId));
    }

    @Override
    public List<FormalAuditResponseDTO> getAllAudits() {
        return auditRepository.findAll().stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<FormalAuditResponseDTO> getAuditsByOfficer(Long officerId) {
        return auditRepository.findByOfficerId(officerId).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<FormalAuditResponseDTO> getAuditsByScope(String scope) {
        return auditRepository.findByScope(scope.toUpperCase()).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<FormalAuditResponseDTO> getAuditsByStatus(FormalAuditStatus status) {
        return auditRepository.findByStatus(status).stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FormalAuditResponseDTO updateAudit(Long auditId, FormalAuditUpdateDTO dto) {
        FormalAudit audit = findById(auditId);
        audit.setFindings(dto.getFindings());
        audit.setStatus(dto.getStatus());
        FormalAudit saved = auditRepository.save(audit);

        logService.createLog(AuditManagementLogRequest.builder()
                .userId(audit.getOfficerId())
                .action("FORMAL_AUDIT_UPDATED")
                .resource("FORMAL_AUDIT:" + auditId)
                .details("Status changed to " + dto.getStatus())
                .timestamp(LocalDateTime.now())
                .build());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "compliance-service", fallbackMethod = "triggerComplianceFallback")
    public FormalAuditResponseDTO triggerComplianceEvaluation(Long auditId) {
        FormalAudit audit = findById(auditId);
        audit.setStatus(FormalAuditStatus.IN_PROGRESS);
        auditRepository.save(audit);

        ComplianceEvaluationResponseDTO result = complianceFeignClient.evaluate(
                audit.getScopeEntityId(),
                audit.getScope(),
                audit.getOfficerId()
        );

        // Null-safe findings population
        String complianceResult = (result.getResult() != null) ? result.getResult() : "UNKNOWN";
        String complianceNotes  = (result.getNotes()  != null) ? result.getNotes()  : "";
        String findings = "[Compliance Result: " + complianceResult + "] " + complianceNotes;
        audit.setFindings(findings);

        if ("FAIL".equalsIgnoreCase(complianceResult)) {
            audit.setStatus(FormalAuditStatus.ESCALATED);
        } else if ("FLAGGED".equalsIgnoreCase(complianceResult)) {
            audit.setStatus(FormalAuditStatus.IN_PROGRESS);
        } else {
            audit.setStatus(FormalAuditStatus.COMPLETED);
        }

        FormalAudit saved = auditRepository.save(audit);

        logService.createLog(AuditManagementLogRequest.builder()
                .userId(audit.getOfficerId())
                .action("COMPLIANCE_EVALUATION_TRIGGERED")
                .resource(audit.getScope() + ":" + audit.getScopeEntityId())
                .details("Result=" + complianceResult + " | " + complianceNotes)
                .timestamp(LocalDateTime.now())
                .build());

        return mapper.toDto(saved);
    }

    // Fallback — safe, no external calls
    public FormalAuditResponseDTO triggerComplianceFallback(Long auditId, Throwable t) {
        if (t instanceof ResourceNotFoundException rne) throw rne;
        if (t instanceof BadRequestException bre) throw bre;
        if (t instanceof FeignException fe) {
            if (fe.status() == 404) throw new ResourceNotFoundException("Entity not found: " + fe.getMessage());
            if (fe.status() == 400) throw new BadRequestException("Bad request: " + fe.getMessage());
        }

        try {
            FormalAudit audit = auditRepository.findById(auditId).orElse(null);
            if (audit != null) {
                audit.setFindings("Compliance service unavailable. Retry when service is restored.");
                audit.setStatus(FormalAuditStatus.IN_PROGRESS);
                return mapper.toDto(auditRepository.save(audit));
            }
        } catch (Exception ignored) {}

        return FormalAuditResponseDTO.builder()
                .auditId(auditId)
                .status(FormalAuditStatus.IN_PROGRESS.name())
                .findings("Compliance service unavailable. Retry when service is restored.")
                .build();
    }

    private FormalAudit findById(Long auditId) {
        return auditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FormalAudit not found with id: " + auditId));
    }
}
