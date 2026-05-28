package com.cts.scheme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SchemeRequestDTO {

        @NotBlank
        private String name;
        @NotBlank
        private String description;
        @NotBlank
        private String eligibilityCriteria;
        @NotBlank
        private String benefits;
        @NotNull
        private Double maxCoverageAmount;
        @NotNull(message = "Validity years is required")
        @Positive(message = "Validity years must be positive")
        private Integer validityYears;
        @NotBlank(message = "Status is required")
        private String status;

}
