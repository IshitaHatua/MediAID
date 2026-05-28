package com.cts.claim_service.mapper;


import org.springframework.stereotype.Component;
import com.cts.claim_service.dto.ClaimRequestDTO;
import com.cts.claim_service.dto.ClaimResponseDTO;
import com.cts.claim_service.model.Claim;

@Component
public class ClaimMapper {

    public Claim toEntity(ClaimRequestDTO dto) {
        Claim claim = new Claim();
        claim.setSchemeId(dto.getSchemeId());
        claim.setClaimAmount(dto.getClaimAmount());
        return claim;
    }

    public ClaimResponseDTO toDto(Claim claim) {
        ClaimResponseDTO dto = new ClaimResponseDTO();
        dto.setClaimId(claim.getClaimId());
        dto.setCitizenId(claim.getCitizenId());
        dto.setSchemeId(claim.getSchemeId());
        dto.setClaimAmount(claim.getClaimAmount());
        dto.setClaimDate(claim.getClaimDate());
        dto.setDescription(claim.getDescription());
        dto.setStatus(claim.getStatus());
        return dto;
    }
}
