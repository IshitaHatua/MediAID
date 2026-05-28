package com.cts.claim_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimDocumentResponseDTO {

    private Long documentId;
    private Long claimId;
    private String fileName;
    private String filePath;
    private String uploadDate;
    private Long uploadedBy;
}
