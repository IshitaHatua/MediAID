package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.request.CitizenRequestDTO;
import com.cts.dto.response.CitizenResponseDTO;
import com.cts.enums.CitizenStatus;
import com.cts.model.Citizen;

@Component
public class CitizenMapper {

	public Citizen toEntity(CitizenRequestDTO dto)
	{
		Citizen citizen = new Citizen();
		citizen.setName(dto.getName());
		citizen.setDob(dto.getDob());
		citizen.setGender(dto.getGender());
		citizen.setAddress(dto.getAddress());
		citizen.setContactInfo(dto.getContactInfo());
		citizen.setStatus(CitizenStatus.PENDING);
		
		return citizen;
	}
	
	public CitizenResponseDTO toDto(Citizen citizen)
	{
		CitizenResponseDTO dto=new CitizenResponseDTO();
		dto.setCitizenId(citizen.getCitizenId());
		dto.setName(citizen.getName());
		dto.setDob(citizen.getDob());
		dto.setGender(citizen.getGender());
		dto.setAddress(citizen.getAddress());
		dto.setContactInfo(citizen.getContactInfo());
		dto.setStatus(citizen.getStatus());

		return dto;
	}
}
