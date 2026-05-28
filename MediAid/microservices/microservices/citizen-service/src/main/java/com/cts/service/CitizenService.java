package com.cts.service;

import com.cts.dto.request.CitizenRequestDTO;
import com.cts.dto.response.CitizenResponseDTO;
import com.cts.enums.CitizenStatus;
import com.cts.exception.ResourceNotFoundException;
import com.cts.model.Citizen;
import java.util.List;

public interface CitizenService {
    CitizenResponseDTO createCitizen(CitizenRequestDTO citizenRequest);
    CitizenResponseDTO findCitizenById(long id) throws ResourceNotFoundException;
    List<CitizenResponseDTO> getAllCitizens();
    CitizenResponseDTO updateCitizen(long id, CitizenRequestDTO updated) throws ResourceNotFoundException;
    CitizenResponseDTO verifyCitizen(long citizenId, CitizenStatus status) throws ResourceNotFoundException;
    void ensureCitizenIsActive(Citizen citizen);
    CitizenResponseDTO suspendCitizen(Long citizenId) throws ResourceNotFoundException;
}
