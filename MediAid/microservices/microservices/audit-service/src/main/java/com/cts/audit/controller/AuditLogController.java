package com.cts.audit.controller;

import com.cts.audit.api.APIResponse;
import com.cts.audit.dto.AuditLogRequest;
import com.cts.audit.model.AuditLog;
import com.cts.audit.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Internal endpoint - called by other microservices to persist audit events.
     * Permitted without JWT (no external user token available between services).
     */
    @PostMapping("/internal/log")
    public ResponseEntity<APIResponse<AuditLog>> createLog(@RequestBody AuditLogRequest request) {
        AuditLog log = new AuditLog();
        log.setUserId(request.getUserId());
        log.setAction(request.getAction());
        log.setResource(request.getResource());
        log.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
        log.setDetails(request.getDetails());

        AuditLog saved = auditLogService.createLog(log);
        return ResponseEntity.ok(APIResponse.<AuditLog>builder()
                .status("SUCCESS")
                .message("Audit log created")
                .data(saved)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditLog>>> getAllLogs() {
        List<AuditLog> logs = auditLogService.getAllLogs();
        return ResponseEntity.ok(APIResponse.<List<AuditLog>>builder()
                .status("SUCCESS")
                .message("Audit logs fetched successfully")
                .data(logs)
                .build());
    }

    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditLog>>> getLatest100Logs() {
        List<AuditLog> logs = auditLogService.getLatest100Logs();
        return ResponseEntity.ok(APIResponse.<List<AuditLog>>builder()
                .status("SUCCESS")
                .message("Latest 100 audit logs fetched")
                .data(logs)
                .build());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditLog>>> getLogsByUser(@PathVariable Long userId) {
        List<AuditLog> logs = auditLogService.getLogsByUser(userId);
        return ResponseEntity.ok(APIResponse.<List<AuditLog>>builder()
                .status("SUCCESS")
                .message("Audit logs fetched for user " + userId)
                .data(logs)
                .build());
    }
}
