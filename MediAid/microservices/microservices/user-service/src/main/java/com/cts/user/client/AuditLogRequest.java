package com.cts.user.client;

import java.time.LocalDateTime;

public class AuditLogRequest {

    private Long userId;
    private String action;
    private String resource;
    private LocalDateTime timestamp;

    public AuditLogRequest(Long userId, String action, String resource) {
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.timestamp = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getResource() { return resource; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
