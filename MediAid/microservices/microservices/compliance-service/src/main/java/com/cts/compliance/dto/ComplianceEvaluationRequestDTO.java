package com.cts.compliance.dto;

import com.cts.compliance.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Full evaluation request — caller supplies all data the rule engine needs.
 * compliance-service never calls back to other services to fetch missing fields.
 * This is the /evaluate/full endpoint contract.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceEvaluationRequestDTO {

    @NotNull(message = "entityId cannot be null")
    private Long entityId;

    @NotNull(message = "entityType cannot be null")
    private EntityType entityType;

    @NotNull(message = "requestedBy cannot be null")
    private Long requestedBy;

    // ── CLAIM fields ──────────────────────────────────────────────────────────
    /** Claim or disbursement amount */
    private Double amount;

    /** HOSPITALIZATION or MEDICINE — used only when entityType=CLAIM */
    private String claimType;

    /** Policy expiry date — used for C-4 rule when entityType=CLAIM */
    private LocalDate policyExpiryDate;

    // ── DISBURSEMENT fields ───────────────────────────────────────────────────
    /** The claim this disbursement is linked to — used for D-3 rule */
    private Long linkedClaimId;

    // ── POLICY fields ─────────────────────────────────────────────────────────
    private LocalDate policyEnrollmentDate;
    private Long citizenId;

    // ── SCHEME fields (populated via SchemeFeignClient enrichment) ────────────
    /** Scheme's maxCoverageAmount — used as the claim and disbursement ceiling */
    private Double schemeMaxCoverage;

    /** Scheme's validityYears — used to verify policy duration does not exceed it */
    private Integer schemeValidityYears;

    /** True when scheme status is ACTIVE — gates all entity-specific rules */
    private Boolean schemeActive;
}
