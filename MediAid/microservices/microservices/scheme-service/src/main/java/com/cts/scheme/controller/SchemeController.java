package com.cts.scheme.controller;

import com.cts.scheme.api.APIResponse;
import com.cts.scheme.dto.SchemeRequestDTO;
import com.cts.scheme.dto.SchemeResponseDTO;
import com.cts.scheme.dto.SchemeStatusUpdateDTO;
import com.cts.scheme.service.SchemeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
public class SchemeController {

    private final SchemeService schemeService;

    public SchemeController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER', 'MANAGER')")
    @GetMapping
    public ResponseEntity<APIResponse<List<SchemeResponseDTO>>> getAllSchemes() {
        List<SchemeResponseDTO> dtos = schemeService.getAllSchemes();
        return ResponseEntity.ok(APIResponse.<List<SchemeResponseDTO>>builder()
                .status("SUCCESS").message("Schemes retrieved").data(dtos).build());
    }

    @PreAuthorize("hasAnyRole('CITIZEN', 'OFFICER', 'MANAGER')")
    @GetMapping("/{schemeId}")
    public ResponseEntity<APIResponse<SchemeResponseDTO>> getScheme(@PathVariable Long schemeId) {
        SchemeResponseDTO responseDTO = schemeService.getSchemeById(schemeId);
        return ResponseEntity.ok(APIResponse.<SchemeResponseDTO>builder()
                .status("SUCCESS").message("Scheme retrieved").data(responseDTO).build());
    }

    /**
     * Internal endpoint — called by compliance-service (and other microservices) via Feign.
     * Permitted without auth in SecurityConfig under /api/schemes/internal/**.
     */
    @GetMapping("/internal/{schemeId}")
    public ResponseEntity<APIResponse<SchemeResponseDTO>> getSchemeInternal(
            @PathVariable Long schemeId) {
        SchemeResponseDTO responseDTO = schemeService.getSchemeById(schemeId);
        return ResponseEntity.ok(APIResponse.<SchemeResponseDTO>builder()
                .status("SUCCESS").message("Scheme retrieved internally").data(responseDTO).build());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<APIResponse<SchemeResponseDTO>> createScheme(
            @Valid @RequestBody SchemeRequestDTO dto) {
        SchemeResponseDTO responseDTO = schemeService.createScheme(dto);
        return ResponseEntity.ok(APIResponse.<SchemeResponseDTO>builder()
                .status("SUCCESS").message("Scheme created successfully").data(responseDTO).build());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{schemeId}/status")
    public ResponseEntity<APIResponse<SchemeResponseDTO>> updateSchemeStatus(
            @PathVariable Long schemeId,
            @Valid @RequestBody SchemeStatusUpdateDTO dto) {
        SchemeResponseDTO responseDTO = schemeService.updateSchemeStatus(schemeId, dto);
        return ResponseEntity.ok(APIResponse.<SchemeResponseDTO>builder()
                .status("SUCCESS").message("Scheme status updated to " + responseDTO.getStatus())
                .data(responseDTO).build());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{schemeId}")
    public ResponseEntity<APIResponse<Void>> deleteScheme(@PathVariable Long schemeId) {
        schemeService.deleteScheme(schemeId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status("SUCCESS").message("Scheme deleted successfully").data(null).build());
    }
}
