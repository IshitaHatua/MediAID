package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.request.CitizenDocumentRequestDTO;
import com.cts.dto.response.CitizenDocumentResponseDTO;
import com.cts.enums.DocumentVerificationStatus;
import com.cts.model.CitizenDocument;

@Component
public class CitizenDocumentMapper {
	
	public CitizenDocument toEntity(CitizenDocumentRequestDTO dto, String fileUri) {
	    CitizenDocument document = new CitizenDocument();
	    document.setDocType(dto.getDocType());
	    document.setUploadedDate(dto.getUploadedDate());
	    document.setFileUri(fileUri);      // ← stored path from FileStorageService
	    document.setVerificationStatus(DocumentVerificationStatus.PENDING);
	    return document;
	}

	public CitizenDocumentResponseDTO toDto(CitizenDocument entity)
	{
		CitizenDocumentResponseDTO dto=new CitizenDocumentResponseDTO();
		dto.setDocumentId(entity.getDocumentId());
		dto.setDocType(entity.getDocType());
		dto.setFileUri(entity.getFileUri());
		dto.setUploadedDate(entity.getUploadedDate());
		dto.setVerificationStatus(entity.getVerificationStatus());
		
		return dto;
	}
}
