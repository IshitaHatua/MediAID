package com.cts.auditmanagement.dto;

import com.cts.auditmanagement.model.FormalAudit.FormalAuditStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class FormalAuditUpdateDTO {
    private String findings;

    @NotNull(message = "status cannot be null")
    private FormalAuditStatus status;
}
