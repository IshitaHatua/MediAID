package com.cts.compliance.model;

import com.cts.compliance.enums.ComplianceResult;
import com.cts.compliance.enums.EntityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_record", indexes = {
        @Index(name = "idx_cr_entity", columnList = "entityId"),
        @Index(name = "idx_cr_type",   columnList = "entityType"),
        @Index(name = "idx_cr_result", columnList = "result")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complianceId;

    @Column(nullable = false, updatable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EntityType entityType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceResult result;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private Long requestedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.evaluatedAt == null) {
            this.evaluatedAt = LocalDateTime.now();
        }
    }
}
