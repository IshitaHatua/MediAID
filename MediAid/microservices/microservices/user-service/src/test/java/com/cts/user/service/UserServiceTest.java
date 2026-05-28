package com.cts.user.service;

import com.cts.user.client.AuditServiceClient;
import com.cts.user.dto.UserResponseDTO;
import com.cts.user.dto.UserRoleUpdateRequestDTO;
import com.cts.user.dto.UserUpdateRequestDTO;
import com.cts.user.exception.ResourceNotFoundException;
import com.cts.user.model.Role;
import com.cts.user.model.User;
import com.cts.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuditServiceClient auditServiceClient;
    @InjectMocks private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setName("Alice");
        existingUser.setEmail("alice@example.com");
        existingUser.setRole(Role.CITIZEN);
    }

    @Test
    void getAllUsers_returnsMappedDtos() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("alice@example.com", result.get(0).getEmail());
        assertEquals(Role.CITIZEN, result.get(0).getRole());
    }

    @Test
    void getUserById_returnsDto_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        UserResponseDTO result = userService.getUserById(1L);

        assertEquals(1L, result.getUserId());
        assertEquals("Alice", result.getName());
    }

    @Test
    void getUserById_throws_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void updateUser_persistsChangesAndLogsAudit() {
        UserUpdateRequestDTO req = new UserUpdateRequestDTO();
        req.setName("Alice Smith");
        req.setRole(Role.OFFICER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO updated = userService.updateUser(1L, req);

        assertEquals("Alice Smith", updated.getName());
        assertEquals(Role.OFFICER, updated.getRole());
        verify(auditServiceClient).log(1L, "UPDATE_USER", "UserService");
    }

    @Test
    void deleteUser_removesUserAndLogsAudit() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(existingUser);
        verify(auditServiceClient).log(1L, "DELETE_USER", "UserService");
    }

    @Test
    void deleteUser_throwsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateUserRole_changesRoleAndLogsAudit() {
        UserRoleUpdateRequestDTO req = new UserRoleUpdateRequestDTO();
        req.setRole(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO updated = userService.updateUserRole(1L, req);

        assertEquals(Role.ADMIN, updated.getRole());
        verify(auditServiceClient).log(1L, "UPDATE_ROLE", "UserService");
    }
}
