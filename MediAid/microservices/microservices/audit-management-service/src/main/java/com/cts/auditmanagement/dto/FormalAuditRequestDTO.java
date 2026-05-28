package com.cts.auditmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class FormalAuditRequestDTO {

    @NotNull(message = "officerId cannot be null")
    private Long officerId;

    @NotBlank(message = "scope cannot be blank — use CLAIM, POLICY, or DISBURSEMENT")
    private String scope;

    @NotNull(message = "scopeEntityId cannot be null")
    private Long scopeEntityId;

    private String findings;
}
