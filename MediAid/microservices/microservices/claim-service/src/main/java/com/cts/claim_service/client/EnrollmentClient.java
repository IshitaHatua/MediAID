package com.cts.claim_service.client;

import com.cts.claim_service.api.APIResponse;
import com.cts.claim_service.dto.EnrollmentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ENROLLMENT-SERVICE")
public interface EnrollmentClient {

    @GetMapping("/api/enrollments/validate")
    Boolean validateEnrollment(@RequestParam Long citizenId, @RequestParam Long schemeId);

    @GetMapping("/api/enrollments/enrollment-status")
    String getEnrollmentStatus(@RequestParam Long citizenId, @RequestParam Long schemeId);

    @GetMapping("/api/enrollments/details")
    APIResponse<EnrollmentResponseDTO> getEnrollmentDetails(
            @RequestParam Long citizenId, @RequestParam Long schemeId);
}
