package com.cts.claim_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.claim_service.model.ClaimValidation;

import java.util.List;

public interface ClaimValidationRepository extends JpaRepository<ClaimValidation, Long> {
    List<ClaimValidation> findByClaimId(Long claimId);
}
