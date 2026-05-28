package com.cts.auditmanagement.service;

import com.cts.auditmanagement.dto.FormalAuditRequestDTO;
import com.cts.auditmanagement.dto.FormalAuditResponseDTO;
import com.cts.auditmanagement.dto.FormalAuditUpdateDTO;
import com.cts.auditmanagement.model.FormalAudit.FormalAuditStatus;

import java.util.List;

public interface FormalAuditService {
    FormalAuditResponseDTO createAudit(FormalAuditRequestDTO dto);
    FormalAuditResponseDTO getAuditById(Long auditId);
    List<FormalAuditResponseDTO> getAllAudits();
    List<FormalAuditResponseDTO> getAuditsByOfficer(Long officerId);
    List<FormalAuditResponseDTO> getAuditsByScope(String scope);
    List<FormalAuditResponseDTO> getAuditsByStatus(FormalAuditStatus status);
    FormalAuditResponseDTO updateAudit(Long auditId, FormalAuditUpdateDTO dto);
    FormalAuditResponseDTO triggerComplianceEvaluation(Long auditId);
}
