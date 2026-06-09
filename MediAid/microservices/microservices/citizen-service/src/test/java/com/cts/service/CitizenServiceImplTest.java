package com.cts.service;

import com.cts.client.AuditServiceClient;
import com.cts.dto.request.CitizenRequestDTO;
import com.cts.dto.response.CitizenResponseDTO;
import com.cts.enums.CitizenStatus;
import com.cts.enums.DocumentVerificationStatus;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.CitizenMapper;
import com.cts.model.Citizen;
import com.cts.model.CitizenDocument;
import com.cts.repository.CitizenDocumentRepository;
import com.cts.repository.CitizenRepository;
import com.cts.security.CurrentUserUtil;
import com.cts.service.serviceImpl.CitizenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitizenServiceImplTest {

    @Mock private CitizenRepository citizenRepository;
    @Mock private CitizenDocumentRepository citizenDocumentRepository;  // added
    @Mock private CitizenMapper citizenMapper;
    @Mock private AuditServiceClient auditServiceClient;
    @Mock private CurrentUserUtil currentUserUtil;

    @InjectMocks
    private CitizenServiceImpl citizenService;

    private Citizen citizen;
    private CitizenRequestDTO requestDTO;
    private CitizenResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        citizen = new Citizen();
        citizen.setCitizenId(1L);
        citizen.setName("John Doe");
        citizen.setDob("1990-01-01");
        citizen.setGender("Male");
        citizen.setAddress("123 Main St");
        citizen.setContactInfo("9876543210");
        citizen.setStatus(CitizenStatus.PENDING);

        requestDTO = new CitizenRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setDob("1990-01-01");
        requestDTO.setGender("Male");
        requestDTO.setAddress("123 Main St");
        requestDTO.setContactInfo("9876543210");

        responseDTO = new CitizenResponseDTO();
        responseDTO.setCitizenId(1L);
        responseDTO.setName("John Doe");
        responseDTO.setStatus(CitizenStatus.PENDING);
    }

    // --- createCitizen ---

    @Test
    void createCitizen_success() {
        when(currentUserUtil.getUserId()).thenReturn(1L);
        when(citizenMapper.toEntity(requestDTO)).thenReturn(citizen);
        when(citizenRepository.save(citizen)).thenReturn(citizen);
        when(citizenMapper.toDto(citizen)).thenReturn(responseDTO);

        CitizenResponseDTO result = citizenService.createCitizen(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getCitizenId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(CitizenStatus.PENDING);
        verify(citizenRepository).save(citizen);
        verify(auditServiceClient).log(1L, "CREATE", "Citizen");
    }

    // --- getAllCitizens ---

    @Test
    void getAllCitizens_returnsList() {
        when(citizenRepository.findAll()).thenReturn(List.of(citizen));
        when(citizenMapper.toDto(any(Citizen.class))).thenReturn(responseDTO);

        List<CitizenResponseDTO> result = citizenService.getAllCitizens();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCitizenId()).isEqualTo(1L);
    }

    @Test
    void getAllCitizens_empty_returnsEmptyList() {
        when(citizenRepository.findAll()).thenReturn(Collections.emptyList());

        List<CitizenResponseDTO> result = citizenService.getAllCitizens();

        assertThat(result).isEmpty();
    }

    // --- findCitizenById ---

    @Test
    void findCitizenById_found() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(citizenMapper.toDto(citizen)).thenReturn(responseDTO);

        CitizenResponseDTO result = citizenService.findCitizenById(1L);

        assertThat(result.getCitizenId()).isEqualTo(1L);
    }

    @Test
    void findCitizenById_notFound_throwsException() {
        when(citizenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citizenService.findCitizenById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Citizen not found");
    }

    // --- updateCitizen ---

    @Test
    void updateCitizen_success() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any(Citizen.class))).thenReturn(citizen);
        when(citizenMapper.toDto(citizen)).thenReturn(responseDTO);
        when(currentUserUtil.getUserId()).thenReturn(1L);

        CitizenResponseDTO result = citizenService.updateCitizen(1L, requestDTO);

        assertThat(result).isNotNull();
        verify(citizenRepository).save(citizen);
        verify(auditServiceClient).log(1L, "UPDATE", "Citizen");
    }

    @Test
    void updateCitizen_notFound_throwsException() {
        when(citizenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citizenService.updateCitizen(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCitizen_suspended_throwsIllegalState() {
        citizen.setStatus(CitizenStatus.SUSPENDED);
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));

        assertThatThrownBy(() -> citizenService.updateCitizen(1L, requestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Suspend citizen cannot update profile");
    }

    // --- verifyCitizen ---

    @Test
    void verifyCitizen_pendingToVerified_success() {
        CitizenDocument doc = new CitizenDocument();
        doc.setVerificationStatus(DocumentVerificationStatus.PENDING);

        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any())).thenReturn(citizen);
        when(citizenDocumentRepository.findByCitizenCitizenId(1L)).thenReturn(List.of(doc));
        when(citizenMapper.toDto(any(Citizen.class))).thenReturn(responseDTO);
        when(currentUserUtil.getUserId()).thenReturn(2L);

        citizenService.verifyCitizen(1L, CitizenStatus.VERIFIED);

        assertThat(citizen.getStatus()).isEqualTo(CitizenStatus.VERIFIED);
        assertThat(doc.getVerificationStatus()).isEqualTo(DocumentVerificationStatus.VERIFIED);
        verify(citizenDocumentRepository).saveAll(anyList());
        verify(auditServiceClient).log(2L, "VERIFY", "Citizen");
    }

    @Test
    void verifyCitizen_pendingToRejected_success() {
        CitizenDocument doc = new CitizenDocument();
        doc.setVerificationStatus(DocumentVerificationStatus.PENDING);

        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any())).thenReturn(citizen);
        when(citizenDocumentRepository.findByCitizenCitizenId(1L)).thenReturn(List.of(doc));
        when(citizenMapper.toDto(any(Citizen.class))).thenReturn(responseDTO);
        when(currentUserUtil.getUserId()).thenReturn(2L);

        citizenService.verifyCitizen(1L, CitizenStatus.REJECTED);

        assertThat(citizen.getStatus()).isEqualTo(CitizenStatus.REJECTED);
        assertThat(doc.getVerificationStatus()).isEqualTo(DocumentVerificationStatus.REJECTED);
    }

    @Test
    void verifyCitizen_noDocs_noSaveAll() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any())).thenReturn(citizen);
        when(citizenDocumentRepository.findByCitizenCitizenId(1L)).thenReturn(Collections.emptyList());
        when(citizenMapper.toDto(any(Citizen.class))).thenReturn(responseDTO);
        when(currentUserUtil.getUserId()).thenReturn(2L);

        citizenService.verifyCitizen(1L, CitizenStatus.VERIFIED);

        assertThat(citizen.getStatus()).isEqualTo(CitizenStatus.VERIFIED);
        verify(citizenDocumentRepository, never()).saveAll(anyList());
    }

    @Test
    void verifyCitizen_notPending_throwsBadRequest() {
        citizen.setStatus(CitizenStatus.VERIFIED);
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));

        assertThatThrownBy(() -> citizenService.verifyCitizen(1L, CitizenStatus.VERIFIED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only pending citizens can be verified or rejected");
    }

    @Test
    void verifyCitizen_suspended_throwsBadRequest() {
        citizen.setStatus(CitizenStatus.SUSPENDED);
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));

        assertThatThrownBy(() -> citizenService.verifyCitizen(1L, CitizenStatus.VERIFIED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Suspended citizen cannot be verified");
    }

    @Test
    void verifyCitizen_invalidStatus_throwsBadRequest() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));

        assertThatThrownBy(() -> citizenService.verifyCitizen(1L, CitizenStatus.PENDING))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invalid verification status");
    }

    // --- suspendCitizen ---

    @Test
    void suspendCitizen_success() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any())).thenReturn(citizen);
        when(citizenMapper.toDto(any(Citizen.class))).thenReturn(responseDTO);
        when(currentUserUtil.getUserId()).thenReturn(2L);

        citizenService.suspendCitizen(1L);

        assertThat(citizen.getStatus()).isEqualTo(CitizenStatus.SUSPENDED);
        verify(auditServiceClient).log(2L, "SUSPEND", "Citizen");
    }

    @Test
    void suspendCitizen_alreadySuspended_throwsBadRequest() {
        citizen.setStatus(CitizenStatus.SUSPENDED);
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(citizen));

        assertThatThrownBy(() -> citizenService.suspendCitizen(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Citizen is already suspended");
    }

    @Test
    void suspendCitizen_notFound_throwsException() {
        when(citizenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citizenService.suspendCitizen(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- ensureCitizenIsActive ---

    @Test
    void ensureCitizenIsActive_suspended_throwsBadRequest() {
        citizen.setStatus(CitizenStatus.SUSPENDED);

        assertThatThrownBy(() -> citizenService.ensureCitizenIsActive(citizen))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Citizen account is suspended");
    }

    @Test
    void ensureCitizenIsActive_notSuspended_noException() {
        citizen.setStatus(CitizenStatus.VERIFIED);

        assertThatCode(() -> citizenService.ensureCitizenIsActive(citizen))
                .doesNotThrowAnyException();
    }
}
