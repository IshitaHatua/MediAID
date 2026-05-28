package com.cts.dto.response;

import com.cts.enums.DocumentVerificationStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CitizenDocumentResponseDTO {

	private Long documentId;
	private String docType;
	private String fileUri;
	private String uploadedDate;
	private DocumentVerificationStatus verificationStatus;
}
