package com.cts.controller;

import com.cts.dto.request.CitizenRequestDTO;
import com.cts.dto.response.CitizenResponseDTO;
import com.cts.enums.CitizenStatus;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.service.CitizenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitizenController.class)
@AutoConfigureMockMvc(addFilters = false)
class CitizenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitizenService citizenService;

    @Autowired
    private ObjectMapper objectMapper;

    private CitizenRequestDTO requestDTO;
    private CitizenResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new CitizenRequestDTO();
        requestDTO.setName("Jane Doe");
        requestDTO.setDob("1995-06-15");
        requestDTO.setGender("Female");
        requestDTO.setAddress("456 Elm Street");
        requestDTO.setContactInfo("9123456789");

        responseDTO = new CitizenResponseDTO();
        responseDTO.setCitizenId(1L);
        responseDTO.setName("Jane Doe");
        responseDTO.setStatus(CitizenStatus.PENDING);
    }

    // --- GET /api/citizens (OFFICER / MANAGER / ADMIN) ---

    @Test
    @WithMockUser(roles = "OFFICER")
    void getAllCitizens_officer_returns200() throws Exception {
        when(citizenService.getAllCitizens()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/citizens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].citizenId").value(1));
    }

    @Test
    @WithMockUser(roles = "OFFICER")
    void getAllCitizens_empty_returns200() throws Exception {
        when(citizenService.getAllCitizens()).thenReturn(List.of());

        mockMvc.perform(get("/api/citizens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // --- POST /api/citizens ---

    @Test
    @WithMockUser
    void createCitizen_returns201() throws Exception {
        when(citizenService.createCitizen(any(CitizenRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/citizens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Citizen registered successfully"))
                .andExpect(jsonPath("$.data.citizenId").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void createCitizen_invalidRequest_returns400() throws Exception {
        CitizenRequestDTO invalid = new CitizenRequestDTO();

        mockMvc.perform(post("/api/citizens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    // --- GET /api/citizens/{citizenId} ---

    @Test
    @WithMockUser
    void findCitizenById_found_returns200() throws Exception {
        when(citizenService.findCitizenById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/citizens/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.citizenId").value(1))
                .andExpect(jsonPath("$.data.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser
    void findCitizenById_notFound_returns404() throws Exception {
        when(citizenService.findCitizenById(99L))
                .thenThrow(new ResourceNotFoundException("Citizen not found"));

        mockMvc.perform(get("/api/citizens/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    // --- PUT /api/citizens/{citizenId} ---

    @Test
    @WithMockUser
    void updateCitizen_success_returns200() throws Exception {
        when(citizenService.updateCitizen(eq(1L), any(CitizenRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/citizens/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Citizen profile updated successfully"))
                .andExpect(jsonPath("$.data.citizenId").value(1));
    }

    @Test
    @WithMockUser
    void updateCitizen_notFound_returns404() throws Exception {
        when(citizenService.updateCitizen(eq(99L), any(CitizenRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Citizen not found"));

        mockMvc.perform(put("/api/citizens/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateCitizen_suspended_returns400() throws Exception {
        when(citizenService.updateCitizen(eq(1L), any(CitizenRequestDTO.class)))
                .thenThrow(new IllegalStateException("Suspend citizen cannot update profile"));

        mockMvc.perform(put("/api/citizens/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    // --- PUT /api/citizens/{citizenId}/verify (OFFICER only) ---

    @Test
    @WithMockUser(roles = "OFFICER")
    void verifyCitizen_officer_returns200() throws Exception {
        responseDTO.setStatus(CitizenStatus.VERIFIED);
        when(citizenService.verifyCitizen(1L, CitizenStatus.VERIFIED)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/citizens/1/verify")
                        .param("status", "VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Citizen verification completed"))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));
    }

    @Test
    @WithMockUser(roles = "OFFICER")
    void verifyCitizen_notPending_returns400() throws Exception {
        when(citizenService.verifyCitizen(eq(1L), any()))
                .thenThrow(new BadRequestException("Only pending citizens can be verified or rejected"));

        mockMvc.perform(put("/api/citizens/1/verify")
                        .param("status", "VERIFIED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    // --- PUT /api/citizens/{citizenId}/suspend (ADMIN only) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void suspendCitizen_admin_returns200() throws Exception {
        responseDTO.setStatus(CitizenStatus.SUSPENDED);
        when(citizenService.suspendCitizen(1L)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/citizens/1/suspend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Citizen account suspended successfully"))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void suspendCitizen_alreadySuspended_returns400() throws Exception {
        when(citizenService.suspendCitizen(1L))
                .thenThrow(new BadRequestException("Citizen is already suspended"));

        mockMvc.perform(put("/api/citizens/1/suspend"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}
