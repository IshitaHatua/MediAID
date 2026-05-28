package com.cts.dto.request;

import com.cts.validation.ValidDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CitizenRequestDTO {

	@NotBlank(message="Name is required")
	private String name;
	
	@NotBlank(message="Date of birth is required")
	@ValidDate
	private String dob;
	
	@NotBlank(message="Gender is required")
	private String gender;
	
	@NotBlank(message="Address id required")
	private String address;
	
	@NotBlank(message = "Contact info is required")
	@Pattern(regexp = "^[6-9]\\d{9}$",message="Invalid phone number")
	private String contactInfo;
}
