package com.mediaid.disbursement.repository;

import com.mediaid.disbursement.model.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {

    Optional<Disbursement> findByClaimId(Long claimId);

    List<Disbursement> findByCitizenId(Long citizenId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Disbursement d " +
           "WHERE d.claimId IN :claimIds AND UPPER(d.status) = 'COMPLETED'")
    BigDecimal sumCompletedByClaimIds(@Param("claimIds") List<Long> claimIds);
}
