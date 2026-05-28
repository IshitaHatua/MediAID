package com.cts.auth.client;

import java.time.LocalDateTime;

public class AuditLogRequest {

    private Long userId;
    private String action;
    private String resource;
    private LocalDateTime timestamp;
    private String details;

    /** Existing 3-parameter constructor — keep for backward compatibility. */
    public AuditLogRequest(Long userId, String action, String resource) {
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.timestamp = LocalDateTime.now();
    }

    /** 4-parameter constructor that also accepts a details string. */
    public AuditLogRequest(Long userId, String action, String resource, String details) {
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getResource() { return resource; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
}
