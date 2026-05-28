package com.cts.exception;

import com.cts.api.APIResponse;

import java.net.MalformedURLException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.<Void>builder()
                        .status("ERROR")
                        .message(errorMessage)
                        .data(null)
                        .build());
    }

    @ExceptionHandler({ BadRequestException.class, IllegalArgumentException.class })
    public ResponseEntity<APIResponse<Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<APIResponse<Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse<Object>> handleConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("Resource already exists"));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error("You do not have permission to access this resource"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGeneric() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("Internal server error"));
    }

    
    @ExceptionHandler(MalformedURLException.class)
    public ResponseEntity<APIResponse<Object>> handleMalformedURL(MalformedURLException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("Invalid file path: " + ex.getMessage()));
    }
    
    @ExceptionHandler
    public ResponseEntity<APIResponse<Object>> handleFileStorage(FileStorageException ex){
    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    			.body(error(ex.getMessage()));
    }
    
    private APIResponse<Object> error(String message) {
        return APIResponse.builder().status("ERROR").message(message).data(null).build();
    }
}