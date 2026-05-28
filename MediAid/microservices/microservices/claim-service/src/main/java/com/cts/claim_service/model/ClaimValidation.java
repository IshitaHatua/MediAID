package com.cts.claim_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ClaimValidation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long validationId;

    @Column(nullable = false)
    private Long claimId;
    @Column(nullable = false)
    private Long officerId;
    private String validationDate;

    private String result;

    @Column(length = 500)
    private String remarks;
}
