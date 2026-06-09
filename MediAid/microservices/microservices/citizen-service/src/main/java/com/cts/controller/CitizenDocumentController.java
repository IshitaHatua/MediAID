package com.cts.controller;

import com.cts.api.APIResponse;
import com.cts.dto.request.CitizenDocumentRequestDTO;
import com.cts.dto.response.CitizenDocumentResponseDTO;
import com.cts.service.CitizenDocumentService;
import com.cts.service.FileStorageService;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CitizenDocumentController {

    private final CitizenDocumentService citizenDocumentService;
    private final FileStorageService fileStorageService;

    public CitizenDocumentController(CitizenDocumentService citizenDocumentService,
                                     FileStorageService fileStorageService) {
        this.citizenDocumentService = citizenDocumentService;
        this.fileStorageService = fileStorageService;
    }

    // Only CITIZEN can upload — service enforces ownership
    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping(value = "/citizens/{citizenId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<CitizenDocumentResponseDTO>> uploadDocument(
            @PathVariable long citizenId,
            @Valid @ModelAttribute CitizenDocumentRequestDTO dto) {

        CitizenDocumentResponseDTO responseDTO = citizenDocumentService.uploadDocument(citizenId, dto);

        APIResponse<CitizenDocumentResponseDTO> response = APIResponse.<CitizenDocumentResponseDTO>builder()
                .status("SUCCESS")
                .message("Document uploaded successfully")
                .data(responseDTO)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // CITIZEN can view their own docs; OFFICER can view any
    @PreAuthorize("hasRole('CITIZEN') or hasRole('OFFICER')")
    @GetMapping("/citizens/{citizenId}/documents")
    public ResponseEntity<APIResponse<List<CitizenDocumentResponseDTO>>> getDocuments(@PathVariable long citizenId) {

        List<CitizenDocumentResponseDTO> documents = citizenDocumentService.getAllDocument(citizenId);

        APIResponse<List<CitizenDocumentResponseDTO>> response = APIResponse.<List<CitizenDocumentResponseDTO>>builder()
                .status("SUCCESS")
                .message("Document fetched successfully")
                .data(documents)
                .build();

        return ResponseEntity.ok(response);
    }

    // Only CITIZEN can delete — service enforces ownership
    @PreAuthorize("hasRole('CITIZEN')")
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<APIResponse<Void>> deleteDocument(@PathVariable long documentId) {
        citizenDocumentService.deleteDocument(documentId);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status("SUCCESS")
                .message("Document deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    // Authenticated users only — removed permitAll
    @PreAuthorize("hasAnyRole('CITIZEN','OFFICER','ADMIN','MANAGER')")
    @GetMapping("/documents/{fileName}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
