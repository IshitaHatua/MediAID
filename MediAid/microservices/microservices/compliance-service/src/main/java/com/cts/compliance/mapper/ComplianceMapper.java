package com.cts.compliance.mapper;

import com.cts.compliance.dto.ComplianceRecordResponseDTO;
import com.cts.compliance.model.ComplianceRecord;
import org.springframework.stereotype.Component;

@Component
public class ComplianceMapper {

    public ComplianceRecordResponseDTO toDto(ComplianceRecord record) {
        if (record == null) return null;
        return ComplianceRecordResponseDTO.builder()
                .complianceId(record.getComplianceId())
                .entityId(record.getEntityId())
                .entityType(record.getEntityType() != null ? record.getEntityType().name() : null)
                .result(record.getResult() != null ? record.getResult().name() : null)
                .notes(record.getNotes())
                .requestedBy(record.getRequestedBy())
                .evaluatedAt(record.getEvaluatedAt())
                .build();
    }
}
