package com.cts.auditmanagement.repository;

import com.cts.auditmanagement.model.FormalAudit;
import com.cts.auditmanagement.model.FormalAudit.FormalAuditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormalAuditRepository extends JpaRepository<FormalAudit, Long> {
    List<FormalAudit> findByOfficerId(Long officerId);
    List<FormalAudit> findByScope(String scope);
    List<FormalAudit> findByStatus(FormalAuditStatus status);
    List<FormalAudit> findByScopeEntityId(Long scopeEntityId);
}
