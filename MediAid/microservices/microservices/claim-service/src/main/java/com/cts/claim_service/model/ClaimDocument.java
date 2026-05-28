package com.cts.claim_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "claim_document")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @Column(nullable = false)
    private Long claimId;

    private String fileName;

    @Column(nullable = false, length = 1000)
    private String filePath;

    private String uploadDate;

    private Long uploadedBy;
}
