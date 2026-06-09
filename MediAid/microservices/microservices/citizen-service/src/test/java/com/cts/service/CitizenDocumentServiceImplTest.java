package com.cts.service;

import com.cts.client.AuditServiceClient;
import com.cts.dto.request.CitizenDocumentRequestDTO;
import com.cts.dto.response.CitizenDocumentResponseDTO;
import com.cts.enums.CitizenStatus;
import com.cts.enums.DocumentVerificationStatus;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.CitizenDocumentMapper;
import com.cts.model.Citizen;
import com.cts.model.CitizenDocument;
import com.cts.repository.CitizenDocumentRepository;
import com.cts.repository.CitizenRepository;
import com.cts.security.CurrentUserUtil;
import com.cts.service.serviceImpl.CitizenDocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitizenDocumentServiceImplTest {

    @Mock private CitizenDocumentRepository docRepo;
    @Mock private CitizenRepository citizenRepo;
    @Mock private CitizenService citizenService;
    @Mock private CitizenDocumentMapper citizenDocumentMapper;
    @Mock private FileStorageService fileStorageService;
    @Mock private AuditServiceClient auditServiceClient;
    @Mock private CurrentUserUtil currentUserUtil;

    @InjectMocks
    private CitizenDocumentServiceImpl documentService;

    private Citizen citizen;
    private CitizenDocument document;
    private CitizenDocumentRequestDTO requestDTO;
    private CitizenDocumentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        citizen = new Citizen();
        citizen.setCitizenId(1L);
        citizen.setStatus(CitizenStatus.VERIFIED);

        document = new CitizenDocument();
        document.setDocumentId(10L);
        document.setDocType("Aadhaar");
        document.setFileUri("uploads/uuid_aadhaar.pdf");
        document.setUploadedDate("2024-01-15");
        document.setVerificationStatus(DocumentVerificationStatus.PENDING);
        document.setCitizen(citizen);

        requestDTO = new CitizenDocumentRequestDTO();
        requestDTO.setDocType("Aadhaar");
        requestDTO.setUploadedDate("2024-01-15");
        requestDTO.setFile(new MockMultipartFile("file", "aadhaar.pdf", "application/pdf", new byte[]{1, 2, 3}));

        responseDTO = new CitizenDocumentResponseDTO();
        responseDTO.setDocumentId(10L);
        responseDTO.setDocType("Aadhaar");
        responseDTO.setFileUri("uploads/uuid_aadhaar.pdf");
        responseDTO.setVerificationStatus(DocumentVerificationStatus.PENDING);
    }

    // --- uploadDocument ---

    @Test
    void uploadDocument_success() {
        when(citizenRepo.findById(1L)).thenReturn(Optional.of(citizen));
        when(fileStorageService.storeFile(any())).thenReturn("uploads/uuid_aadhaar.pdf");
        when(citizenDocumentMapper.toEntity(requestDTO, "uploads/uuid_aadhaar.pdf")).thenReturn(document);
        when(docRepo.save(document)).thenReturn(document);
        when(citizenDocumentMapper.toDto(document)).thenReturn(responseDTO);
        when(currentUserUtil.getUserId()).thenReturn(1L);

        CitizenDocumentResponseDTO result = documentService.uploadDocument(1L, requestDTO);

        assertThat(result.getDocumentId()).isEqualTo(10L);
        verify(docRepo).save(document);
        verify(auditServiceClient).log(1L, "UPLOAD", "CitizenDocument");
    }

    @Test
    void uploadDocument_citizenNotFound_throwsException() {
        when(citizenRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.uploadDocument(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Citizen not found");
    }

    @Test
    void uploadDocument_suspendedCitizen_throwsBadRequest() {
        when(citizenRepo.findById(1L)).thenReturn(Optional.of(citizen));
        doThrow(new BadRequestException("Citizen account is suspended"))
                .when(citizenService).ensureCitizenIsActive(citizen);

        assertThatThrownBy(() -> documentService.uploadDocument(1L, requestDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Citizen account is suspended");
    }

    // --- getAllDocument ---

    @Test
    void getAllDocument_success() {
        when(citizenRepo.findById(1L)).thenReturn(Optional.of(citizen));
        when(docRepo.findByCitizenCitizenId(1L)).thenReturn(List.of(document));
        when(citizenDocumentMapper.toDto(document)).thenReturn(responseDTO);

        List<CitizenDocumentResponseDTO> result = documentService.getAllDocument(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentId()).isEqualTo(10L);
    }

    @Test
    void getAllDocument_citizenNotFound_throwsException() {
        when(citizenRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getAllDocument(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllDocument_suspendedCitizen_throwsBadRequest() {
        when(citizenRepo.findById(1L)).thenReturn(Optional.of(citizen));
        doThrow(new BadRequestException("Citizen account is suspended"))
                .when(citizenService).ensureCitizenIsActive(citizen);

        assertThatThrownBy(() -> documentService.getAllDocument(1L))
                .isInstanceOf(BadRequestException.class);
    }

}
