package com.cts.auth.controller;

import com.cts.auth.api.APIResponse;
import com.cts.auth.dto.AuthResponseDTO;
import com.cts.auth.dto.ForgotPasswordRequest;
import com.cts.auth.dto.LoginRequestDTO;
import com.cts.auth.dto.RegisterRequestDTO;
import com.cts.auth.dto.ResetPasswordRequest;
import com.cts.auth.dto.UserResponseDTO;
import com.cts.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponse<Object>> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok(APIResponse.builder()
                .status("SUCCESS")
                .message("User registered successfully")
                .data(null)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(APIResponse.<AuthResponseDTO>builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(response)
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(APIResponse.builder()
                .status("SUCCESS")
                .message("OTP sent to your email")
                .data(null)
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(APIResponse.builder()
                .status("SUCCESS")
                .message("Password reset successfully")
                .data(null)
                .build());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<UserResponseDTO>>> listUsers() {
        List<UserResponseDTO> users = authService.getAllUsers();
        return ResponseEntity.ok(APIResponse.<List<UserResponseDTO>>builder()
                .status("SUCCESS")
                .message("Users fetched successfully")
                .data(users)
                .build());
    }
}
