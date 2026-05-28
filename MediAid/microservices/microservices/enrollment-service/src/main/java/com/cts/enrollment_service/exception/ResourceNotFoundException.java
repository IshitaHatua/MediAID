package com.cts.enrollment_service.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String messege) {
        super(messege);
    }

}