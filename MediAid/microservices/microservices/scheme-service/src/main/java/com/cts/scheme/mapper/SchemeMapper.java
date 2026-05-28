package com.cts.scheme.mapper;


import com.cts.scheme.dto.SchemeRequestDTO;
import org.springframework.stereotype.Component;
import com.cts.scheme.dto.SchemeResponseDTO;
import com.cts.scheme.model.Scheme;

@Component
public class SchemeMapper {

    public SchemeResponseDTO toDto(Scheme scheme) {
        SchemeResponseDTO dto = new SchemeResponseDTO();
        dto.setSchemeId(scheme.getSchemeId());
        dto.setName(scheme.getName());
        dto.setDescription(scheme.getDescription());
        dto.setEligibilityCriteria(scheme.getEligibilityCriteria());
        dto.setBenefits(scheme.getBenefits());
        dto.setMaxCoverageAmount(scheme.getMaxCoverageAmount());
        dto.setValidityYears(scheme.getValidityYears());
        dto.setStatus(scheme.getStatus());
        return dto;
    }

    public Scheme toEntity(SchemeRequestDTO dto){
        Scheme scheme=new Scheme();
        scheme.setName(dto.getName());
        scheme.setDescription(dto.getDescription());
        scheme.setEligibilityCriteria(dto.getEligibilityCriteria());
        scheme.setBenefits(dto.getBenefits());
        scheme.setMaxCoverageAmount(dto.getMaxCoverageAmount());
        scheme.setValidityYears(dto.getValidityYears());
        scheme.setStatus(dto.getStatus());
        return scheme;
    }
}
