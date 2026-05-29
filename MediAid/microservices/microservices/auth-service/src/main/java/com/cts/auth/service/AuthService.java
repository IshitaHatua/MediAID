package com.cts.auth.service;

import com.cts.auth.dto.AuthResponseDTO;
import com.cts.auth.dto.ForgotPasswordRequest;
import com.cts.auth.dto.LoginRequestDTO;
import com.cts.auth.dto.RegisterRequestDTO;
import com.cts.auth.dto.ResetPasswordRequest;
import com.cts.auth.dto.UserResponseDTO;

import java.util.List;

public interface AuthService {

    void register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    List<UserResponseDTO> getAllUsers();
}
