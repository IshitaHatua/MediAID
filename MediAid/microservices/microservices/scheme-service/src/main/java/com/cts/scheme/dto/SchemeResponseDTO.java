package com.cts.scheme.dto;


import lombok.Data;


@Data
public class SchemeResponseDTO {

    private Long schemeId;
    private String name;
    private String description;
    private String eligibilityCriteria;
    private String benefits;
    private Double maxCoverageAmount;
    private int validityYears;
    private String status;
}