package com.cts.compliance.repository;

import com.cts.compliance.enums.ComplianceResult;
import com.cts.compliance.enums.EntityType;
import com.cts.compliance.model.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {
    List<ComplianceRecord> findByEntityId(Long entityId);
    List<ComplianceRecord> findByEntityType(EntityType entityType);
    List<ComplianceRecord> findByResult(ComplianceResult result);
    List<ComplianceRecord> findByRequestedBy(Long requestedBy);
    List<ComplianceRecord> findByEntityIdAndEntityType(Long entityId, EntityType entityType);
}
