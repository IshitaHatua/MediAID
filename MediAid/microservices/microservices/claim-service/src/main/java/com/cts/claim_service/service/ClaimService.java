package com.cts.claim_service.service;

import com.cts.claim_service.dto.ClaimDocumentResponseDTO;
import com.cts.claim_service.dto.ClaimRequestDTO;
import com.cts.claim_service.dto.ClaimResponseDTO;
import com.cts.claim_service.dto.ClaimStatusUpdateDTO;
import com.cts.claim_service.model.ClaimValidation;
import com.cts.claim_service.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClaimService {
    ClaimResponseDTO createClaim(Long citizenId, ClaimRequestDTO dto);
    ClaimResponseDTO getClaimById(Long claimId) throws ResourceNotFoundException;
    List<ClaimResponseDTO> getClaimsByCitizen(Long citizenId);
    List<ClaimResponseDTO> getAllClaims();
    ClaimResponseDTO updateClaimStatus(Long claimId, ClaimStatusUpdateDTO dto, Long officerId) throws ResourceNotFoundException;
    List<ClaimValidation> getClaimsValidations();
    ClaimDocumentResponseDTO uploadDocument(Long claimId, Long citizenId, MultipartFile file);
    List<ClaimDocumentResponseDTO> getDocumentsByClaimId(Long claimId);
    Long getCitizenIdByClaimId(Long claimId);
    Double getClaimAmountByClaimId(Long claimId);
}
