package com.cts.user.controller;

import com.cts.user.api.APIResponse;
import com.cts.user.dto.UserResponseDTO;
import com.cts.user.dto.UserRoleUpdateRequestDTO;
import com.cts.user.dto.UserUpdateRequestDTO;
import com.cts.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(APIResponse.<List<UserResponseDTO>>builder()
                .status("SUCCESS")
                .message("Users fetched successfully")
                .data(users)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(APIResponse.<UserResponseDTO>builder()
                .status("SUCCESS")
                .message("User fetched successfully")
                .data(user)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserResponseDTO>> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        UserResponseDTO updated = userService.updateUser(id, request);
        return ResponseEntity.ok(APIResponse.<UserResponseDTO>builder()
                .status("SUCCESS")
                .message("User updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(APIResponse.builder()
                .status("SUCCESS")
                .message("User deleted successfully")
                .data(null)
                .build());
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserResponseDTO>> updateUserRole(@PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequestDTO request) {
        UserResponseDTO updated = userService.updateUserRole(id, request);
        return ResponseEntity.ok(APIResponse.<UserResponseDTO>builder()
                .status("SUCCESS")
                .message("User role updated successfully")
                .data(updated)
                .build());
    }
}
