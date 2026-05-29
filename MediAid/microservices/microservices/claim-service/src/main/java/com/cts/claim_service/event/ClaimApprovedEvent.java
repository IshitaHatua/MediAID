package com.cts.claim_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Published from inside updateClaimStatus when an officer approves a claim.
 * Consumed by a {@link org.springframework.transaction.event.TransactionalEventListener}
 * (phase = AFTER_COMMIT) so the disbursement creation only fires once the
 * APPROVED status is durably committed and visible to other services calling
 * back via Feign.
 */
@Getter
@AllArgsConstructor
public class ClaimApprovedEvent {
    private final Long claimId;
}
