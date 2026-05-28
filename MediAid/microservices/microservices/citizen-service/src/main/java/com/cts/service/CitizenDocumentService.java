package com.cts.service;

import java.util.List;


import com.cts.dto.request.CitizenDocumentRequestDTO;
import com.cts.dto.response.CitizenDocumentResponseDTO;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;

public interface CitizenDocumentService {
    CitizenDocumentResponseDTO uploadDocument(long citizenId,CitizenDocumentRequestDTO citizenDocumentRequestDTO) throws ResourceNotFoundException;
    List<CitizenDocumentResponseDTO> getAllDocument(long citizenId) throws ResourceNotFoundException;
    CitizenDocumentResponseDTO verifyDocuments(long documentId, String status) throws BadRequestException, ResourceNotFoundException;
    void deleteDocument(long documentId) throws ResourceNotFoundException;
}