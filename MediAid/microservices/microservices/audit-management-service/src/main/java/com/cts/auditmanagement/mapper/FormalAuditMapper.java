package com.cts.auditmanagement.mapper;

import com.cts.auditmanagement.dto.FormalAuditRequestDTO;
import com.cts.auditmanagement.dto.FormalAuditResponseDTO;
import com.cts.auditmanagement.model.FormalAudit;
import com.cts.auditmanagement.model.FormalAudit.FormalAuditStatus;
import org.springframework.stereotype.Component;

@Component
public class FormalAuditMapper {

    public FormalAudit toEntity(FormalAuditRequestDTO dto) {
        return FormalAudit.builder()
                .officerId(dto.getOfficerId())
                .scope(dto.getScope() != null ? dto.getScope().toUpperCase() : null)
                .scopeEntityId(dto.getScopeEntityId())
                .findings(dto.getFindings())
                .status(FormalAuditStatus.PLANNED)
                .build();
    }

    public FormalAuditResponseDTO toDto(FormalAudit audit) {
        return FormalAuditResponseDTO.builder()
                .auditId(audit.getAuditId())
                .officerId(audit.getOfficerId())
                .scope(audit.getScope())
                .scopeEntityId(audit.getScopeEntityId())
                .findings(audit.getFindings())
                .createdAt(audit.getCreatedAt())
                .status(audit.getStatus() != null ? audit.getStatus().name() : null)
                .build();
    }
}
