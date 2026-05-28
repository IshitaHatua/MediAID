package com.cts.scheme.serviceImpl;

import com.cts.scheme.dto.SchemeRequestDTO;
import com.cts.scheme.dto.SchemeResponseDTO;
import com.cts.scheme.dto.SchemeStatusUpdateDTO;
import com.cts.scheme.exception.BadRequestException;
import com.cts.scheme.exception.ResourceNotFoundException;
import com.cts.scheme.mapper.SchemeMapper;
import com.cts.scheme.model.Scheme;
import com.cts.scheme.repository.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemeServiceImplTest {

    @Mock private SchemeRepository schemeRepository;
    @Mock private SchemeMapper schemeMapper;

    @InjectMocks private SchemeServiceImpl schemeService;

    private Scheme scheme;
    private SchemeResponseDTO schemeDto;

    @BeforeEach
    void setUp() {
        scheme = new Scheme();
        scheme.setSchemeId(1L);
        scheme.setName("Ayushman Bharat");
        scheme.setMaxCoverageAmount(500000.0);
        scheme.setValidityYears(1);
        scheme.setStatus("ACTIVE");

        schemeDto = new SchemeResponseDTO();
        schemeDto.setSchemeId(1L);
        schemeDto.setName("Ayushman Bharat");
        schemeDto.setStatus("ACTIVE");
    }

    @Test
    void getAllSchemes_returnsMappedList() {
        when(schemeRepository.findAll()).thenReturn(List.of(scheme));
        when(schemeMapper.toDto(scheme)).thenReturn(schemeDto);

        List<SchemeResponseDTO> result = schemeService.getAllSchemes();

        assertEquals(1, result.size());
        assertEquals("Ayushman Bharat", result.get(0).getName());
    }

    @Test
    void getSchemeById_returnsScheme_whenFound() {
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(schemeMapper.toDto(scheme)).thenReturn(schemeDto);

        SchemeResponseDTO result = schemeService.getSchemeById(1L);

        assertEquals(1L, result.getSchemeId());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void getSchemeById_throws_whenNotFound() {
        when(schemeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> schemeService.getSchemeById(99L));
    }

    @Test
    void createScheme_savesAndReturnsMappedDto() {
        SchemeRequestDTO req = new SchemeRequestDTO();
        req.setName("New Scheme");
        req.setStatus("ACTIVE");
        req.setMaxCoverageAmount(100000.0);
        req.setValidityYears(2);

        when(schemeMapper.toEntity(req)).thenReturn(scheme);
        when(schemeRepository.save(scheme)).thenReturn(scheme);
        when(schemeMapper.toDto(scheme)).thenReturn(schemeDto);

        SchemeResponseDTO result = schemeService.createScheme(req);

        assertNotNull(result);
        verify(schemeRepository).save(scheme);
    }

    @Test
    void updateSchemeStatus_acceptsValidStatus() {
        SchemeStatusUpdateDTO dto = new SchemeStatusUpdateDTO();
        dto.setStatus("INACTIVE");

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(schemeRepository.save(any(Scheme.class))).thenReturn(scheme);
        when(schemeMapper.toDto(any(Scheme.class))).thenReturn(schemeDto);

        SchemeResponseDTO result = schemeService.updateSchemeStatus(1L, dto);

        assertNotNull(result);
        assertEquals("INACTIVE", scheme.getStatus());
    }

    @Test
    void updateSchemeStatus_rejectsInvalidStatus() {
        SchemeStatusUpdateDTO dto = new SchemeStatusUpdateDTO();
        dto.setStatus("ARCHIVED");

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));

        assertThrows(BadRequestException.class, () -> schemeService.updateSchemeStatus(1L, dto));
        verify(schemeRepository, never()).save(any());
    }

    @Test
    void deleteScheme_deletesWhenFound() {
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        schemeService.deleteScheme(1L);
        verify(schemeRepository).delete(scheme);
    }

    @Test
    void deleteScheme_throwsWhenMissing() {
        when(schemeRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> schemeService.deleteScheme(7L));
    }
}
