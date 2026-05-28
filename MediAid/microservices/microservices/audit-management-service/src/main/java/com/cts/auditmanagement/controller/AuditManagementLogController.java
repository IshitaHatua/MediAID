package com.cts.auditmanagement.controller;

import com.cts.auditmanagement.api.APIResponse;
import com.cts.auditmanagement.dto.AuditManagementLogRequest;
import com.cts.auditmanagement.model.AuditManagementLog;
import com.cts.auditmanagement.service.AuditManagementLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-management")
public class AuditManagementLogController {

    private final AuditManagementLogService logService;

    public AuditManagementLogController(AuditManagementLogService logService) {
        this.logService = logService;
    }

    /**
     * Internal endpoint — called by compliance-service via Feign to write
     * activity entries after compliance evaluations.
     * Security: permitAll (declared in SecurityConfig). Unchanged.
     */
    @PostMapping("/internal/log")
    public ResponseEntity<APIResponse<AuditManagementLog>> createLog(
            @RequestBody AuditManagementLogRequest request) {
        AuditManagementLog saved = logService.createLog(request);
        return ResponseEntity.ok(APIResponse.<AuditManagementLog>builder()
                .status("SUCCESS")
                .message("Audit management log created")
                .data(saved)
                .build());
    }

    // COMPLIANCE and AUDITOR added: both roles need full visibility into activity logs
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OFFICER','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditManagementLog>>> getAllLogs() {
        return ResponseEntity.ok(APIResponse.<List<AuditManagementLog>>builder()
                .status("SUCCESS")
                .message("Audit management logs fetched")
                .data(logService.getAllLogs())
                .build());
    }

    @GetMapping("/logs/latest")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OFFICER','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditManagementLog>>> getLatest100Logs() {
        return ResponseEntity.ok(APIResponse.<List<AuditManagementLog>>builder()
                .status("SUCCESS")
                .message("Latest 100 audit management logs fetched")
                .data(logService.getLatest100Logs())
                .build());
    }

    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OFFICER','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditManagementLog>>> getLogsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(APIResponse.<List<AuditManagementLog>>builder()
                .status("SUCCESS")
                .message("Logs for user " + userId)
                .data(logService.getLogsByUser(userId))
                .build());
    }

    @GetMapping("/logs/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OFFICER','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditManagementLog>>> getLogsByAction(
            @PathVariable String action) {
        return ResponseEntity.ok(APIResponse.<List<AuditManagementLog>>builder()
                .status("SUCCESS")
                .message("Logs for action: " + action)
                .data(logService.getLogsByAction(action))
                .build());
    }

    @GetMapping("/logs/resource/{fragment}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OFFICER','COMPLIANCE','AUDITOR')")
    public ResponseEntity<APIResponse<List<AuditManagementLog>>> getLogsByResource(
            @PathVariable String fragment) {
        return ResponseEntity.ok(APIResponse.<List<AuditManagementLog>>builder()
                .status("SUCCESS")
                .message("Logs for resource containing: " + fragment)
                .data(logService.getLogsByResource(fragment))
                .build());
    }
}
