package com.cts.auth.service;

import com.cts.auth.client.AuditServiceClient;
import com.cts.auth.dto.AuthResponseDTO;
import com.cts.auth.dto.ForgotPasswordRequest;
import com.cts.auth.dto.LoginRequestDTO;
import com.cts.auth.dto.RegisterRequestDTO;
import com.cts.auth.dto.ResetPasswordRequest;
import com.cts.auth.dto.UserResponseDTO;
import com.cts.auth.exception.ResourceNotFoundException;
import com.cts.auth.model.Role;
import com.cts.auth.model.User;
import com.cts.auth.repository.UserRepository;
import com.cts.auth.util.EmailValidator;
import com.cts.auth.util.JwtUtils;
import com.cts.auth.util.OtpUtil;
import com.cts.auth.util.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuditServiceClient auditServiceClient;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils, AuditServiceClient auditServiceClient,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.auditServiceClient = auditServiceClient;
        this.emailService = emailService;
    }

    public void register(RegisterRequestDTO request) {
        EmailValidator.validate(request.getEmail());
        PasswordValidator.validate(request.getPassword());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CITIZEN);

        User savedUser = userRepository.save(user);

        auditServiceClient.log(savedUser.getUserId(), "REGISTER",
                "USER:" + savedUser.getUserId(),
                "User registered — email=" + savedUser.getEmail()
                        + " role=" + savedUser.getRole());
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(),user.getUserId());

        auditServiceClient.log(user.getUserId(), "LOGIN",
                "USER:" + user.getUserId(),
                "User logged in — email=" + user.getEmail()
                        + " role=" + user.getRole());

        return new AuthResponseDTO(token, user.getRole().name());
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String otp = OtpUtil.generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        auditServiceClient.log(user.getUserId(), "FORGOT_PASSWORD",
                "USER:" + user.getUserId(),
                "Password reset requested — email=" + user.getEmail());
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponseDTO(u.getUserId(), u.getName(), u.getEmail(),
                        u.getRole() != null ? u.getRole().name() : null))
                .collect(Collectors.toList());
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        PasswordValidator.validate(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        auditServiceClient.log(user.getUserId(), "RESET_PASSWORD",
                "USER:" + user.getUserId(),
                "Password reset completed — email=" + user.getEmail());
    }
}
