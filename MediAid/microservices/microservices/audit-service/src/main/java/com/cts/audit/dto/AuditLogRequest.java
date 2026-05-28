package com.cts.audit.dto;

import java.time.LocalDateTime;

public class AuditLogRequest {

    private Long userId;
    private String action;
    private String resource;
    private LocalDateTime timestamp;
    private String details;

    public AuditLogRequest() {}

    public AuditLogRequest(Long userId, String action, String resource, LocalDateTime timestamp) {
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.timestamp = timestamp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
