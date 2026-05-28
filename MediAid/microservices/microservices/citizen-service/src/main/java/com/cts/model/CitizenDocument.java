package com.cts.model;

import com.cts.enums.DocumentVerificationStatus;
import com.cts.validation.ValidDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CitizenDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @NotBlank(message = "Document type is required")
    private String docType;

    @Column(name="file_uri")
    private String fileUri;

    @NotBlank(message = "Uploaded date is required")
    @ValidDate
    private String uploadedDate;

    @NotNull(message="Status is required")
    @Enumerated(EnumType.STRING)
    private DocumentVerificationStatus verificationStatus;

    @ManyToOne
    @JoinColumn(name = "citizenId")
    @JsonIgnore
    private Citizen citizen;

}