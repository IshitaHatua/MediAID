package com.cts.controller;

import com.cts.api.APIResponse;
import com.cts.dto.request.CitizenDocumentRequestDTO;
import com.cts.dto.response.CitizenDocumentResponseDTO;
import com.cts.exception.ResourceNotFoundException;
import com.cts.service.CitizenDocumentService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;


@RestController
@RequestMapping("/api")
public class CitizenDocumentController {

    private final CitizenDocumentService citizenDocumentService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    public CitizenDocumentController(CitizenDocumentService citizenDocumentService) {
        this.citizenDocumentService = citizenDocumentService;
    }

    @PostMapping(value = "/citizens/{citizenId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<CitizenDocumentResponseDTO>> uploadDocument(
            @PathVariable long citizenId,
            @Valid @ModelAttribute CitizenDocumentRequestDTO dto) {  // ← @ModelAttribute

        CitizenDocumentResponseDTO responseDTO = citizenDocumentService.uploadDocument(citizenId, dto);

        APIResponse<CitizenDocumentResponseDTO> response = APIResponse.<CitizenDocumentResponseDTO>builder()
                .status("SUCCESS")
                .message("Document uploaded successfully")
                .data(responseDTO)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PreAuthorize("hasRole('CITIZEN') or hasRole('OFFICER')")
    @GetMapping("/citizens/{citizenId}/documents")
    public ResponseEntity<APIResponse<List<CitizenDocumentResponseDTO>>> getDocuments(@PathVariable long citizenId){

        List<CitizenDocumentResponseDTO> documents = citizenDocumentService.getAllDocument(citizenId);

        APIResponse<List<CitizenDocumentResponseDTO>> response = APIResponse.<List<CitizenDocumentResponseDTO>>builder()
                .status("SUCCESS")
                .message("Document fetched successfully")
                .data(documents)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PutMapping("/documents/{documentId}/verify")
    public ResponseEntity<APIResponse<CitizenDocumentResponseDTO>> verifyDocuments(@PathVariable long documentId,
                                           @RequestParam String status){

        CitizenDocumentResponseDTO responseDTO = citizenDocumentService.verifyDocuments(documentId, status);

        APIResponse<CitizenDocumentResponseDTO> response = APIResponse.<CitizenDocumentResponseDTO>builder()
                .status("SUCCESS")
                .message("Document verification status updated")
                .data(responseDTO)
                .build();

        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasRole('CITIZEN') or hasRole('OFFICER')")
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<APIResponse<Void>> deleteDocument(@PathVariable long documentId) {
        citizenDocumentService.deleteDocument(documentId);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status("SUCCESS")
                .message("Document deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/documents/{fileName}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
    }
}