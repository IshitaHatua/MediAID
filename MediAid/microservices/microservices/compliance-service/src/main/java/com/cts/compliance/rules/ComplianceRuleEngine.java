package com.cts.compliance.rules;

import com.cts.compliance.dto.ComplianceEvaluationRequestDTO;
import com.cts.compliance.enums.ComplianceResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Compliance rule engine — thresholds are read from the scheme fetched at
 * enrichment time (schemeMaxCoverage, schemeValidityYears, schemeActive) rather
 * than hardcoded constants.  Only the universal medical floor
 * (HOSPITALIZATION_MIN_FLOOR) is kept as a constant because it is not
 * scheme-specific.
 *
 * Design decisions:
 * - All inputs are null-checked before use — no NullPointerException can escape.
 * - S-0 (scheme-active check) runs first.  If the scheme is INACTIVE the result
 *   is returned immediately and no further entity rules run.
 * - claimType (C-3) is skipped with PASS when not supplied, because Claim entity
 *   has no claimType field yet (planned as future team change).
 * - Result: PASS = all rules pass, FLAGGED = 1 violation, FAIL = 2+ violations.
 * - Adding a new rule: add one private method + one call in the relevant
 *   evaluate*() method.
 */
@Component
public class ComplianceRuleEngine {

    private static final double HOSPITALIZATION_MIN_FLOOR = 500.0;

    // ── Public entry point ────────────────────────────────────────────────────

    public RuleResult evaluate(ComplianceEvaluationRequestDTO req) {
        if (req == null) {
            return new RuleResult(ComplianceResult.FLAGGED, "VIOLATIONS: Request is null.");
        }
        if (req.getEntityType() == null) {
            return new RuleResult(ComplianceResult.FLAGGED, "VIOLATIONS: entityType is null.");
        }

        List<String> violations = new ArrayList<>();
        List<String> passes     = new ArrayList<>();

        // S-0: Scheme must be ACTIVE before any entity rules run
        checkSchemeActive(req, violations, passes);
        if (Boolean.FALSE.equals(req.getSchemeActive())) {
            return buildResult(violations, passes);
        }

        return switch (req.getEntityType()) {
            case CLAIM        -> evaluateClaim(req, violations, passes);
            case POLICY       -> evaluatePolicy(req, violations, passes);
            case DISBURSEMENT -> evaluateDisbursement(req, violations, passes);
        };
    }

    // ── Scheme active check ───────────────────────────────────────────────────

    private void checkSchemeActive(ComplianceEvaluationRequestDTO req,
            List<String> violations, List<String> passes) {
        if (req.getSchemeActive() == null) {
            violations.add("S-0: Scheme status could not be verified — scheme data unavailable");
        } else if (!req.getSchemeActive()) {
            violations.add("S-0: The scheme is INACTIVE");
        } else {
            passes.add("S-0: Scheme is ACTIVE");
        }
    }

    // ── CLAIM rules ───────────────────────────────────────────────────────────

    private RuleResult evaluateClaim(ComplianceEvaluationRequestDTO req,
            List<String> violations, List<String> passes) {

        // C-1: Amount must be positive
        if (req.getAmount() == null || req.getAmount() <= 0) {
            violations.add("C-1: Claim amount must be greater than zero");
        } else {
            passes.add("C-1: Claim amount is positive");
        }

        // C-2: Amount must not exceed scheme coverage ceiling
        if (req.getSchemeMaxCoverage() == null) {
            violations.add("C-2: Claim ceiling could not be verified — scheme data unavailable");
        } else if (req.getAmount() != null && req.getAmount() > req.getSchemeMaxCoverage()) {
            violations.add(String.format(
                    "C-2: Claim amount %.2f exceeds scheme ceiling of %.2f",
                    req.getAmount(), req.getSchemeMaxCoverage()));
        } else if (req.getAmount() != null && req.getAmount() > 0) {
            passes.add(String.format(
                    "C-2: Claim amount %.2f is within scheme ceiling of %.2f",
                    req.getAmount(), req.getSchemeMaxCoverage()));
        }

        // C-3: Hospitalization minimum floor
        // Skipped with PASS when claimType is null (no field on Claim entity yet)
        if (req.getClaimType() != null && !req.getClaimType().isBlank()) {
            if ("HOSPITALIZATION".equalsIgnoreCase(req.getClaimType())) {
                if (req.getAmount() != null && req.getAmount() < HOSPITALIZATION_MIN_FLOOR) {
                    violations.add(String.format(
                            "C-3: Hospitalization claim %.2f is below minimum floor %.2f",
                            req.getAmount(), HOSPITALIZATION_MIN_FLOOR));
                } else {
                    passes.add("C-3: Hospitalization claim meets minimum floor");
                }
            }
        } else {
            // claimType not yet available — skip rule, treat as pass
            passes.add("C-3: claimType not supplied — rule skipped (future field)");
        }

        // C-4: Policy must not be expired
        if (req.getPolicyExpiryDate() != null) {
            if (req.getPolicyExpiryDate().isBefore(LocalDate.now())) {
                violations.add("C-4: The associated policy has expired on " + req.getPolicyExpiryDate());
            } else {
                passes.add("C-4: Policy is still active until " + req.getPolicyExpiryDate());
            }
        } else {
            passes.add("C-4: Policy expiry not supplied — rule skipped");
        }

        return buildResult(violations, passes);
    }

    // ── POLICY rules ──────────────────────────────────────────────────────────

    private RuleResult evaluatePolicy(ComplianceEvaluationRequestDTO req,
            List<String> violations, List<String> passes) {

        // P-1: Policy must not be expired
        if (req.getPolicyExpiryDate() == null) {
            violations.add("P-1: Policy expiry date is missing");
        } else if (req.getPolicyExpiryDate().isBefore(LocalDate.now())) {
            violations.add("P-1: Policy has expired on " + req.getPolicyExpiryDate());
        } else {
            passes.add("P-1: Policy is active until " + req.getPolicyExpiryDate());
        }

        // P-2a: Enrollment date must precede expiry date
        if (req.getPolicyEnrollmentDate() != null && req.getPolicyExpiryDate() != null) {
            if (!req.getPolicyEnrollmentDate().isBefore(req.getPolicyExpiryDate())) {
                violations.add("P-2a: Enrollment date must be before expiry date");
            } else {
                passes.add("P-2a: Enrollment date precedes expiry date");
            }
        } else {
            passes.add("P-2a: Enrollment date not supplied — rule skipped");
        }

        // P-2b: Policy duration must not exceed scheme validity years
        if (req.getSchemeValidityYears() == null) {
            passes.add("P-2b: Scheme validity years not available — rule skipped");
        } else if (req.getPolicyEnrollmentDate() != null && req.getPolicyExpiryDate() != null) {
            int actualYears = Period.between(
                    req.getPolicyEnrollmentDate(), req.getPolicyExpiryDate()).getYears();
            if (actualYears > req.getSchemeValidityYears()) {
                violations.add(String.format(
                        "P-2b: Policy duration %d year(s) exceeds scheme validity of %d year(s)",
                        actualYears, req.getSchemeValidityYears()));
            } else {
                passes.add(String.format(
                        "P-2b: Policy duration %d year(s) is within scheme validity of %d year(s)",
                        actualYears, req.getSchemeValidityYears()));
            }
        } else {
            passes.add("P-2b: Enrollment or expiry date not supplied — rule skipped");
        }

        // P-3: Policy must be linked to a citizen
        if (req.getCitizenId() == null) {
            violations.add("P-3: Policy must be associated with a citizen");
        } else {
            passes.add("P-3: Policy is linked to citizen " + req.getCitizenId());
        }

        return buildResult(violations, passes);
    }

    // ── DISBURSEMENT rules ────────────────────────────────────────────────────

    private RuleResult evaluateDisbursement(ComplianceEvaluationRequestDTO req,
            List<String> violations, List<String> passes) {

        // D-1: Amount must be positive
        if (req.getAmount() == null || req.getAmount() <= 0) {
            violations.add("D-1: Disbursement amount must be greater than zero");
        } else {
            passes.add("D-1: Disbursement amount is positive");
        }

        // D-2: Amount must not exceed scheme coverage ceiling
        if (req.getSchemeMaxCoverage() == null) {
            violations.add("D-2: Disbursement ceiling could not be verified — scheme data unavailable");
        } else if (req.getAmount() != null && req.getAmount() > req.getSchemeMaxCoverage()) {
            violations.add(String.format(
                    "D-2: Disbursement amount %.2f exceeds scheme ceiling of %.2f",
                    req.getAmount(), req.getSchemeMaxCoverage()));
        } else if (req.getAmount() != null && req.getAmount() > 0) {
            passes.add(String.format(
                    "D-2: Disbursement amount %.2f is within scheme ceiling of %.2f",
                    req.getAmount(), req.getSchemeMaxCoverage()));
        }

        // D-3: Must reference a valid claim
        if (req.getLinkedClaimId() == null) {
            violations.add("D-3: Disbursement must reference a valid claim ID");
        } else {
            passes.add("D-3: Disbursement linked to claim " + req.getLinkedClaimId());
        }

        return buildResult(violations, passes);
    }

    // ── Result builder ────────────────────────────────────────────────────────

    private RuleResult buildResult(List<String> violations, List<String> passes) {
        ComplianceResult result;
        if (violations.isEmpty()) {
            result = ComplianceResult.PASS;
        } else if (violations.size() == 1) {
            result = ComplianceResult.FLAGGED;
        } else {
            result = ComplianceResult.FAIL;
        }

        StringBuilder notes = new StringBuilder();
        if (!passes.isEmpty()) {
            notes.append("PASSED: ").append(String.join("; ", passes)).append(". ");
        }
        if (!violations.isEmpty()) {
            notes.append("VIOLATIONS: ").append(String.join("; ", violations)).append(".");
        }

        return new RuleResult(result, notes.toString().trim());
    }

    // ── Result value object ───────────────────────────────────────────────────

    public record RuleResult(ComplianceResult result, String notes) {}
}
