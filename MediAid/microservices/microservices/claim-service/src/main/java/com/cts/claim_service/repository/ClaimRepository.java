package com.cts.claim_service.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.claim_service.model.Claim;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByStatus(String status);
    List<Claim> findByCitizenId(Long citizenId);
    List<Claim> findByCitizenIdAndSchemeIdAndStatus(Long citizenId, Long schemeId, Claim.ClaimStatus status);
}