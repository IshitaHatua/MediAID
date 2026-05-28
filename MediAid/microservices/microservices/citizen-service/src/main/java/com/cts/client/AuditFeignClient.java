package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="audit-service", fallback=AuditFeignClientFallback.class)
public interface AuditFeignClient {

	@PostMapping("/api/audit/internal/log")
    void log(@RequestBody AuditLogRequest request);
	
}
