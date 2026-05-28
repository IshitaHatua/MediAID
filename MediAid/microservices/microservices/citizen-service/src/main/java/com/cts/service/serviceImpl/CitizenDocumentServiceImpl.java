package com.cts.service.serviceImpl;

import com.cts.client.AuditServiceClient;
import com.cts.dto.request.CitizenDocumentRequestDTO;
import com.cts.dto.response.CitizenDocumentResponseDTO;
import com.cts.enums.DocumentVerificationStatus;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.CitizenDocumentMapper;
import com.cts.model.Citizen;
import com.cts.model.CitizenDocument;
import com.cts.repository.CitizenDocumentRepository;
import com.cts.repository.CitizenRepository;
import com.cts.security.CurrentUserUtil;
import com.cts.service.CitizenDocumentService;
import com.cts.service.CitizenService;
import com.cts.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

@Service
@Validated
public class CitizenDocumentServiceImpl implements CitizenDocumentService {

    private final CitizenDocumentRepository docRepo;
    private final CitizenRepository citizenRepo;
    private final CitizenService citizenService;
    private final CitizenDocumentMapper citizenDocumentMapper;
    private final FileStorageService fileStorageService;
    private final AuditServiceClient auditServiceClient;
    private final CurrentUserUtil currentUserUtil;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public CitizenDocumentServiceImpl(CitizenDocumentRepository docRepo,
                                      CitizenRepository citizenRepo,
                                      CitizenService citizenService,
                                      CitizenDocumentMapper citizenDocumentMapper,
                                      FileStorageService fileStorageService,
                                      AuditServiceClient auditServiceClient,
                                      CurrentUserUtil currentUserUtil) {
        this.docRepo = docRepo;
        this.citizenRepo = citizenRepo;
        this.citizenService = citizenService;
        this.citizenDocumentMapper = citizenDocumentMapper;
        this.fileStorageService = fileStorageService;
        this.auditServiceClient = auditServiceClient;
        this.currentUserUtil = currentUserUtil;
    }

    @Override
    public CitizenDocumentResponseDTO uploadDocument(long citizenId, CitizenDocumentRequestDTO dto)
            throws ResourceNotFoundException {

        Citizen citizen = citizenRepo.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found"));

        citizenService.ensureCitizenIsActive(citizen);

        String fileUri = fileStorageService.storeFile(dto.getFile());
        CitizenDocument document = citizenDocumentMapper.toEntity(dto, fileUri);
        document.setCitizen(citizen);

        CitizenDocument saved = docRepo.save(document);
        auditServiceClient.log(currentUserUtil.getUserId(), "UPLOAD", "CitizenDocument");
        return citizenDocumentMapper.toDto(saved);
    }

    @Override
    public List<CitizenDocumentResponseDTO> getAllDocument(long citizenId)
            throws ResourceNotFoundException {

        Citizen citizen = citizenRepo.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found"));

        citizenService.ensureCitizenIsActive(citizen);

        return docRepo.findByCitizenCitizenId(citizenId)
                .stream()
                .map(citizenDocumentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CitizenDocumentResponseDTO verifyDocuments(long documentId, String status)
            throws BadRequestException, ResourceNotFoundException {

        CitizenDocument doc = docRepo.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id " + documentId));

        try {
            DocumentVerificationStatus verificationStatus =
                    DocumentVerificationStatus.valueOf(status.toUpperCase());
            doc.setVerificationStatus(verificationStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value. Must be: PENDING, VERIFIED, or REJECTED");
        }

        CitizenDocument verified = docRepo.save(doc);
        auditServiceClient.log(currentUserUtil.getUserId(), "VERIFY", "CitizenDocument");
        return citizenDocumentMapper.toDto(verified);
    }

    @Override
    public void deleteDocument(long documentId) throws ResourceNotFoundException {
        CitizenDocument doc = docRepo.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id " + documentId));

        String fileUri = doc.getFileUri();
        docRepo.delete(doc);

        if (fileUri != null && !fileUri.isBlank()) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(fileUri).normalize();
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {
                // file removal best-effort; DB row is already gone
            }
        }

        auditServiceClient.log(currentUserUtil.getUserId(), "DELETE", "CitizenDocument");
    }
}