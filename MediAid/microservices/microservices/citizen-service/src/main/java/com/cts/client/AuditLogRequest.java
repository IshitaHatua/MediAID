package com.cts.client;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
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

    
    

}
