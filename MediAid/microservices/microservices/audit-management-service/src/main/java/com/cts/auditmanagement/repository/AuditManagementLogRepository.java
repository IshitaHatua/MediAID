package com.cts.auditmanagement.repository;

import com.cts.auditmanagement.model.AuditManagementLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditManagementLogRepository extends JpaRepository<AuditManagementLog, Long> {
    List<AuditManagementLog> findByUserId(Long userId);
    List<AuditManagementLog> findByAction(String action);
    List<AuditManagementLog> findByResourceContaining(String resourceFragment);
    List<AuditManagementLog> findTop100ByOrderByTimestampDesc();
}
