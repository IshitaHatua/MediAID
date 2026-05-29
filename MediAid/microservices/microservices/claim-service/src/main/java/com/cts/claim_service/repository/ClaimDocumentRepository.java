package com.cts.claim_service.repository;

import com.cts.claim_service.model.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {
    List<ClaimDocument> findByClaimId(Long claimId);

    // Lookup used by the download endpoint: the frontend identifies a document by
    // its stored fileName (which is timestamp-prefixed, so unique in practice).
    Optional<ClaimDocument> findFirstByFileName(String fileName);
}
