package com.mediaid.disbursement.service;

import com.mediaid.disbursement.dto.request.DisbursementRequestDTO;
import com.mediaid.disbursement.dto.response.DisbursementResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface DisbursementService {
    DisbursementResponseDTO createDisbursement(DisbursementRequestDTO requestDTO);
    DisbursementResponseDTO getByClaimId(Long claimId);
    DisbursementResponseDTO getByDisbursementId(Long disbursementId);
    List<DisbursementResponseDTO> getMyDisbursements(Long citizenId);
    List<DisbursementResponseDTO> getAllDisbursements();
    DisbursementResponseDTO updateDisbursementStatus(Long disbursementId, String status);
    BigDecimal getTotalUtilized(List<Long> claimIds);
}
