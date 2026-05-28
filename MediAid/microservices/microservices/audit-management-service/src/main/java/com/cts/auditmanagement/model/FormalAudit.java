package com.cts.auditmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a formal audit instance initiated by a compliance officer.
 * Tracks scope (CLAIM/POLICY/DISBURSEMENT), findings from compliance evaluation,
 * and lifecycle status.
 *
 * Table name: formal_audit — avoids any conflict with existing audit table.
 */
@Entity
@Table(name = "formal_audit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(nullable = false, updatable = false)
    private Long officerId;

    /** What type of entity is being audited: CLAIM, POLICY, DISBURSEMENT */
    @Column(nullable = false, updatable = false, length = 50)
    private String scope;

    /** The ID of the specific entity being audited */
    @Column(nullable = false, updatable = false)
    private Long scopeEntityId;

    /** Populated after compliance evaluation is triggered */
    @Column(columnDefinition = "TEXT")
    private String findings;

    /** Set once at creation — never overwritten */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormalAuditStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public enum FormalAuditStatus {
        PLANNED, IN_PROGRESS, COMPLETED, ESCALATED
    }
}
