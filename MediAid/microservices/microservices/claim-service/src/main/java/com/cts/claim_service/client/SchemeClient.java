package com.cts.claim_service.client;

import com.cts.claim_service.api.APIResponse;
import com.cts.claim_service.dto.SchemeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SCHEME-SERVICE")
public interface SchemeClient {

    @GetMapping("/api/schemes/{schemeId}")
    APIResponse<SchemeResponseDTO> getSchemeById(@PathVariable("schemeId") Long schemeId);
}
