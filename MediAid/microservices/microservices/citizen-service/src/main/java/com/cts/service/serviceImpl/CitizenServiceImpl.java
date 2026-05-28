package com.cts.service.serviceImpl;

import com.cts.client.AuditServiceClient;
import com.cts.dto.request.CitizenRequestDTO;
import com.cts.dto.response.CitizenResponseDTO;
import com.cts.enums.CitizenStatus;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.CitizenMapper;
import com.cts.model.Citizen;
import com.cts.repository.CitizenRepository;
import com.cts.security.CurrentUserUtil;
import com.cts.service.CitizenService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class CitizenServiceImpl implements CitizenService {

    private final CitizenRepository citizenRepository;
    private final CitizenMapper citizenMapper;
    private final AuditServiceClient auditServiceClient;
    private final CurrentUserUtil currentUserUtil;

    public CitizenServiceImpl(CitizenRepository citizenRepository,
                               CitizenMapper citizenMapper,
                               AuditServiceClient auditServiceClient,
                               CurrentUserUtil currentUserUtil) {
        this.citizenRepository = citizenRepository;
        this.citizenMapper = citizenMapper;
        this.auditServiceClient = auditServiceClient;
        this.currentUserUtil = currentUserUtil;
    }

    @Override
    public CitizenResponseDTO createCitizen(CitizenRequestDTO citizenRequest) {
        Long userId = currentUserUtil.getUserId();
        Citizen citizen = citizenMapper.toEntity(citizenRequest);
        citizen.setCitizenId(userId);
        Citizen saved = citizenRepository.save(citizen);
        auditServiceClient.log(userId, "CREATE", "Citizen");
        return citizenMapper.toDto(saved);
    }

    @Override
    public List<CitizenResponseDTO> getAllCitizens() {
        return citizenRepository.findAll().stream()
                .map(citizenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CitizenResponseDTO findCitizenById(long id) throws ResourceNotFoundException {
        Citizen citizen = citizenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found"));
        return citizenMapper.toDto(citizen);
    }

    @Override
    public CitizenResponseDTO updateCitizen(long id, CitizenRequestDTO updated)
            throws ResourceNotFoundException {

        Citizen existing = citizenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found"));

        if (existing.getStatus() == CitizenStatus.SUSPENDED) {
            throw new IllegalStateException("Suspend citizen cannot update profile");
        }

        existing.setName(updated.getName());
        existing.setDob(updated.getDob());
        existing.setGender(updated.getGender());
        existing.setAddress(updated.getAddress());
        existing.setContactInfo(updated.getContactInfo());

        Citizen saved = citizenRepository.save(existing);
        auditServiceClient.log(currentUserUtil.getUserId(), "UPDATE", "Citizen");
        return citizenMapper.toDto(saved);
    }

    @Override
    public CitizenResponseDTO verifyCitizen(long citizenId, CitizenStatus status)
            throws ResourceNotFoundException {

        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found"));

        if (citizen.getStatus() == CitizenStatus.SUSPENDED) {
            throw new BadRequestException("Suspended citizen cannot be verified");
        }

        if (citizen.getStatus() != CitizenStatus.PENDING) {
            throw new BadRequestException("Only pending citizens can be verified or rejected");
        }

        if (status != CitizenStatus.VERIFIED && status != CitizenStatus.REJECTED) {
            throw new BadRequestException("invalid verification status:" + status);
        }

        citizen.setStatus(status);
        Citizen updated = citizenRepository.save(citizen);
        auditServiceClient.log(currentUserUtil.getUserId(), "VERIFY", "Citizen");
        return citizenMapper.toDto(updated);
    }

    @Override
    public void ensureCitizenIsActive(Citizen citizen) {
        if (citizen.getStatus() == CitizenStatus.SUSPENDED) {
            throw new BadRequestException("Citizen account is suspended");
        }
    }

    @Override
    @Transactional
    public CitizenResponseDTO suspendCitizen(Long citizenId) throws ResourceNotFoundException {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found"));

        if (citizen.getStatus() == CitizenStatus.SUSPENDED) {
            throw new BadRequestException("Citizen is already suspended");
        }

        citizen.setStatus(CitizenStatus.SUSPENDED);
        Citizen suspended = citizenRepository.save(citizen);
        auditServiceClient.log(currentUserUtil.getUserId(), "SUSPEND", "Citizen");
        return citizenMapper.toDto(suspended);
    }
}