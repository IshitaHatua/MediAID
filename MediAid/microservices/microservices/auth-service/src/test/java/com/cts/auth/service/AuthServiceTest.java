package com.cts.auth.service;

import com.cts.auth.client.AuditServiceClient;
import com.cts.auth.dto.AuthResponseDTO;
import com.cts.auth.dto.LoginRequestDTO;
import com.cts.auth.dto.RegisterRequestDTO;
import com.cts.auth.model.Role;
import com.cts.auth.model.User;
import com.cts.auth.repository.UserRepository;
import com.cts.auth.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuditServiceClient auditServiceClient;
    @Mock private EmailService emailService;

    @InjectMocks private AuthService authService;

    private RegisterRequestDTO validRegisterRequest;
    private LoginRequestDTO validLoginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequestDTO();
        validRegisterRequest.setName("Alice");
        validRegisterRequest.setEmail("alice@example.com");
        validRegisterRequest.setPassword("Password@123");

        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setEmail("alice@example.com");
        validLoginRequest.setPassword("Password@123");

        existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setName("Alice");
        existingUser.setEmail("alice@example.com");
        existingUser.setPassword("encoded-password");
        existingUser.setRole(Role.CITIZEN);
    }

    @Test
    void register_persistsNewUserAndWritesAuditLog() {
        when(userRepository.findByEmail(validRegisterRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(99L);
            return u;
        });

        authService.register(validRegisterRequest);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("alice@example.com")
                        && u.getRole() == Role.CITIZEN
                        && "encoded-password".equals(u.getPassword())));
        verify(auditServiceClient).log(eq(99L), eq("REGISTER"), eq("AuthService"));
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(validRegisterRequest));
        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenWhenCredentialsValid() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password@123", "encoded-password")).thenReturn(true);
        when(jwtUtils.generateToken(anyString(), anyString(), any())).thenReturn("signed.jwt.token");

        AuthResponseDTO response = authService.login(validLoginRequest);

        assertEquals("signed.jwt.token", response.getToken());
        assertEquals("CITIZEN", response.getRole());
        verify(auditServiceClient).log(1L, "LOGIN", "AuthService");
    }

    @Test
    void login_throwsForUnknownEmail() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(validLoginRequest));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_throwsForBadPassword() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password@123", "encoded-password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(validLoginRequest));
        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtUtils, never()).generateToken(anyString(), anyString(), any());
    }
}
