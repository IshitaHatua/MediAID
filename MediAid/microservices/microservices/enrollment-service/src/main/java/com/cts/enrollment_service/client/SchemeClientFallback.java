package com.cts.enrollment_service.client;

import com.cts.enrollment_service.api.APIResponse;
import com.cts.enrollment_service.dto.SchemeResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SchemeClientFallback implements SchemeClient {

    private static final Logger log = LoggerFactory.getLogger(SchemeClientFallback.class);

    @Override
    public APIResponse<SchemeResponseDTO> getSchemeById(Long id) {
        log.warn("[EnrollmentService] Scheme Service unavailable — getSchemeById id={}", id);
        throw new RuntimeException("Scheme Service is currently unavailable. Please try again later.");
    }
}
