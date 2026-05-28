package com.cts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cts.api.APIResponse;
import com.cts.dto.request.CitizenRequestDTO;
import com.cts.dto.response.CitizenResponseDTO;
import com.cts.enums.CitizenStatus;
import com.cts.service.CitizenService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    private final CitizenService citizenService;

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @PreAuthorize("hasAnyRole('OFFICER','MANAGER','ADMIN')")
    @GetMapping
    public ResponseEntity<APIResponse<List<CitizenResponseDTO>>> getAllCitizens() {
        List<CitizenResponseDTO> citizens = citizenService.getAllCitizens();
        APIResponse<List<CitizenResponseDTO>> response = APIResponse.<List<CitizenResponseDTO>>builder()
                .status("SUCCESS")
                .message("Citizens fetched successfully")
                .data(citizens)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<APIResponse<CitizenResponseDTO>> createCitizen(@Valid @RequestBody CitizenRequestDTO dto) {
        CitizenResponseDTO responseDTO = citizenService.createCitizen(dto);

        APIResponse<CitizenResponseDTO> response = APIResponse.<CitizenResponseDTO>builder()
                .status("SUCCESS")
                .message("Citizen registered successfully")
                .data(responseDTO)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{citizenId}")
    public ResponseEntity<APIResponse<CitizenResponseDTO>> findCitizenById(@PathVariable long citizenId) {

        CitizenResponseDTO responseDto = citizenService.findCitizenById(citizenId);

        APIResponse<CitizenResponseDTO> response = APIResponse.<CitizenResponseDTO>builder()
                .status("SUCCESS")
                .message("Citizen fetched successfully")
                .data(responseDto)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{citizenId}")
    public ResponseEntity<APIResponse<CitizenResponseDTO>> updateCitizen(@PathVariable long citizenId,
                                 @Valid @RequestBody CitizenRequestDTO dto){

        CitizenResponseDTO responseDto = citizenService.updateCitizen(citizenId, dto);

        APIResponse<CitizenResponseDTO> response = APIResponse.<CitizenResponseDTO>builder()
                .status("SUCCESS")
                .message("Citizen profile updated successfully")
                .data(responseDto)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PutMapping("/{citizenId}/verify")
    public ResponseEntity<APIResponse<CitizenResponseDTO>> verifyCitizen(@PathVariable Long citizenId,
                                                                          @RequestParam CitizenStatus status){

        CitizenResponseDTO responseDTO = citizenService.verifyCitizen(citizenId, status);

        APIResponse<CitizenResponseDTO> response = APIResponse.<CitizenResponseDTO>builder()
                .status("SUCCESS")
                .message("Citizen verification completed")
                .data(responseDTO)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{citizenId}/suspend")
    public ResponseEntity<APIResponse<CitizenResponseDTO>> suspendCitizen(@PathVariable long citizenId){

        CitizenResponseDTO responseDTO = citizenService.suspendCitizen(citizenId);

        APIResponse<CitizenResponseDTO> response = APIResponse.<CitizenResponseDTO>builder()
                .status("SUCCESS")
                .message("Citizen account suspended successfully")
                .data(responseDTO)
                .build();

        return ResponseEntity.ok(response);
    }
}