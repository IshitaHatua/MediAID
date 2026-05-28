package com.cts.dto.response;

import com.cts.enums.CitizenStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CitizenResponseDTO {

	private Long citizenId;
	private String name;
	private String dob;
	private String gender;
	private String address;
	private String contactInfo;
	private CitizenStatus status;

}
