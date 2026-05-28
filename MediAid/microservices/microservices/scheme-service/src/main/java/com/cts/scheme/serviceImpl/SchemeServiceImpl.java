package com.cts.scheme.serviceImpl;

import com.cts.scheme.client.AuditManagementFeignClient;
import com.cts.scheme.dto.AuditManagementLogRequest;
import com.cts.scheme.dto.SchemeRequestDTO;
import com.cts.scheme.dto.SchemeResponseDTO;
import com.cts.scheme.dto.SchemeStatusUpdateDTO;
import com.cts.scheme.exception.BadRequestException;
import com.cts.scheme.exception.ResourceNotFoundException;
import com.cts.scheme.mapper.SchemeMapper;
import com.cts.scheme.model.Scheme;
import com.cts.scheme.repository.SchemeRepository;
import com.cts.scheme.service.SchemeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemeServiceImpl implements SchemeService {

    private static final Logger log = LoggerFactory.getLogger(SchemeServiceImpl.class);

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeMapper schemeMapper;

    @Autowired
    private AuditManagementFeignClient auditManagementFeignClient;

    @Override
    public List<SchemeResponseDTO> getAllSchemes() {
        return schemeRepository.findAll()
                .stream()
                .map(schemeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SchemeResponseDTO createScheme(SchemeRequestDTO dto) {
        Scheme scheme = schemeMapper.toEntity(dto);
        Scheme saved = schemeRepository.save(scheme);
        writeAuditLog(null, "SCHEME_CREATED", "SCHEME:" + saved.getSchemeId(),
                "Scheme created: " + saved.getName() + " maxCoverage=" + saved.getMaxCoverageAmount());
        return schemeMapper.toDto(saved);
    }

    @Override
    public SchemeResponseDTO getSchemeById(Long id) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + id));
        return schemeMapper.toDto(scheme);
    }

    @Override
    public SchemeResponseDTO updateSchemeStatus(Long id, SchemeStatusUpdateDTO dto) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + id));

        String newStatus = dto.getStatus().toUpperCase();
        if (!newStatus.equals("ACTIVE") && !newStatus.equals("INACTIVE")) {
            throw new BadRequestException("Invalid status '" + dto.getStatus() + "'. Allowed values: ACTIVE, INACTIVE.");
        }

        scheme.setStatus(newStatus);
        Scheme saved = schemeRepository.save(scheme);
        writeAuditLog(null, "SCHEME_STATUS_CHANGED", "SCHEME:" + saved.getSchemeId(),
                "Scheme " + saved.getName() + " status changed to " + saved.getStatus());
        return schemeMapper.toDto(saved);
    }

    @Override
    public void deleteScheme(Long id) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + id));
        schemeRepository.delete(scheme);
        writeAuditLog(null, "SCHEME_DELETED", "SCHEME:" + id,
                "Scheme id=" + id + " deleted");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Writes an activity log entry to audit-management-service.
     * Fire-and-forget — never blocks or rolls back the caller's operation.
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
            log.warn("[SchemeService] Could not write audit log for action={} resource={}: {}",
                    action, resource, ex.getMessage());
        }
    }
}
