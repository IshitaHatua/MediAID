package com.mediaid.payment.client;

import com.mediaid.payment.dto.AuditManagementLogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditManagementFeignClientFallback implements AuditManagementFeignClient {

    private static final Logger log = LoggerFactory.getLogger(AuditManagementFeignClientFallback.class);

    @Override
    public void log(AuditManagementLogRequest request) {
        log.warn("[PaymentService] audit-management-service unavailable — audit log NOT written. action={} resource={}",
                request != null ? request.getAction() : "null",
                request != null ? request.getResource() : "null");
    }
}
