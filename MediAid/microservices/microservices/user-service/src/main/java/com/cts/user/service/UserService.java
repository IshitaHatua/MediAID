package com.cts.user.service;

import com.cts.user.client.AuditServiceClient;
import com.cts.user.dto.UserResponseDTO;
import com.cts.user.dto.UserRoleUpdateRequestDTO;
import com.cts.user.dto.UserUpdateRequestDTO;
import com.cts.user.exception.ResourceNotFoundException;
import com.cts.user.model.User;
import com.cts.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditServiceClient auditServiceClient;

    public UserService(UserRepository userRepository, AuditServiceClient auditServiceClient) {
        this.userRepository = userRepository;
        this.auditServiceClient = auditServiceClient;
    }

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(user.getUserId(), user.getName(), user.getEmail(), user.getRole());
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());
        user.setRole(request.getRole());
        User updated = userRepository.save(user);

        auditServiceClient.log(user.getUserId(), "UPDATE_USER", "UserService");

        return toDTO(updated);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);

        auditServiceClient.log(id, "DELETE_USER", "UserService");
    }

    public UserResponseDTO updateUserRole(Long id, UserRoleUpdateRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(request.getRole());
        User updated = userRepository.save(user);

        auditServiceClient.log(user.getUserId(), "UPDATE_ROLE", "UserService");

        return toDTO(updated);
    }
}
