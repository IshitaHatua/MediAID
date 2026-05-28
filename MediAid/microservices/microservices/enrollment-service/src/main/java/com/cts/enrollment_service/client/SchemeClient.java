package com.cts.enrollment_service.client;

import com.cts.enrollment_service.api.APIResponse;
import com.cts.enrollment_service.dto.SchemeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SCHEME-SERVICE")
public interface SchemeClient {

    @GetMapping("/api/schemes/{id}")
    APIResponse<SchemeResponseDTO> getSchemeById(@PathVariable("id") Long id);
}
