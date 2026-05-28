package com.cts.compliance.service;

import com.cts.compliance.dto.ComplianceEvaluationRequestDTO;
import com.cts.compliance.dto.ComplianceEvaluationResponseDTO;
import com.cts.compliance.dto.ComplianceRecordRequestDTO;
import com.cts.compliance.dto.ComplianceRecordResponseDTO;
import com.cts.compliance.enums.EntityType;

import java.util.List;

public interface ComplianceRecordService {

    /** Called by audit-service Feign with minimal context (entityId + entityType only) */
    ComplianceEvaluationResponseDTO evaluateSimple(Long entityId, String entityType, Long requestedBy);

    /** Called directly by officers/gateway with full context — preferred path */
    ComplianceEvaluationResponseDTO evaluateFull(ComplianceEvaluationRequestDTO dto);

    /** Manual check from officer console with minimal fields */
    ComplianceRecordResponseDTO manualCheck(ComplianceRecordRequestDTO dto);

    ComplianceRecordResponseDTO getById(Long complianceId);
    List<ComplianceRecordResponseDTO> getAll();
    List<ComplianceRecordResponseDTO> getByEntityId(Long entityId);
    List<ComplianceRecordResponseDTO> getByEntityType(String entityType);
    List<ComplianceRecordResponseDTO> getViolations();
    List<ComplianceRecordResponseDTO> getFlagged();
}
