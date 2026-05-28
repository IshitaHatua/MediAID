package com.cts.compliance.dto;

import com.cts.compliance.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceRecordRequestDTO {

    @NotNull(message = "entityId cannot be null")
    private Long entityId;

    @NotNull(message = "entityType cannot be null")
    private EntityType entityType;

    @NotNull(message = "requestedBy cannot be null")
    private Long requestedBy;
}
