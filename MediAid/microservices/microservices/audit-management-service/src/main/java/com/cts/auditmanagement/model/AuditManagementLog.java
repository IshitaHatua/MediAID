package com.cts.auditmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable activity log owned by audit-management-service.
 * Stored in audit_management_db — completely separate from the existing
 * audit_log table in the mediAid database (owned by audit-service).
 *
 * Table name: audit_management_log to avoid any naming conflict.
 */
@Entity
@Table(name = "audit_management_log", indexes = {
        @Index(name = "idx_aml_user",   columnList = "userId"),
        @Index(name = "idx_aml_action", columnList = "action")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditManagementLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, updatable = false, length = 100)
    private String action;

    @Column(nullable = false, updatable = false, length = 200)
    private String resource;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onPersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
