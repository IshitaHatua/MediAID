package com.cts.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.cts.validation.ValidDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CitizenDocumentRequestDTO {

	@NotBlank(message = "Document type is required")
	private String docType;
	
	//@NotBlank(message = "File URI is required")
	private String fileUri;
	
	@NotBlank(message = "Uploaded date is required")
	@ValidDate
	private String uploadedDate;
	
	@NotNull(message = "File is required")
	private MultipartFile file;
}
