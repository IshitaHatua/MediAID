package com.cts.scheme.service;

import com.cts.scheme.dto.SchemeRequestDTO;
import com.cts.scheme.dto.SchemeResponseDTO;
import com.cts.scheme.dto.SchemeStatusUpdateDTO;

import java.util.List;

public interface SchemeService {
    List<SchemeResponseDTO> getAllSchemes();
    SchemeResponseDTO getSchemeById(Long id);
    SchemeResponseDTO createScheme(SchemeRequestDTO dto);
    SchemeResponseDTO updateSchemeStatus(Long id, SchemeStatusUpdateDTO dto);
    void deleteScheme(Long id);
}