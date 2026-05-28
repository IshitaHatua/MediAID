package com.mediaid.disbursement.mapper;

import com.mediaid.disbursement.dto.request.DisbursementRequestDTO;
import com.mediaid.disbursement.dto.response.DisbursementResponseDTO;
import com.mediaid.disbursement.model.Disbursement;
import org.springframework.stereotype.Component;

@Component
public class DisbursementMapper {

    public Disbursement toEntity(DisbursementRequestDTO request) {
        if (request == null) return null;
        Disbursement d = new Disbursement();
        d.setAmount(request.getAmount());
        d.setDate(request.getDate());
        d.setStatus(request.getStatus());
        return d;
    }

    public DisbursementResponseDTO toDto(Disbursement disbursement) {
        if (disbursement == null) return null;
        DisbursementResponseDTO dto = new DisbursementResponseDTO();
        dto.setDisbursementId(disbursement.getId());
        dto.setAmount(disbursement.getAmount());
        dto.setDate(disbursement.getDate());
        dto.setStatus(disbursement.getStatus());
        dto.setClaimId(disbursement.getClaimId());
        dto.setCitizenId(disbursement.getCitizenId());
        dto.setSchemeId(disbursement.getSchemeId());
        return dto;
    }
}
