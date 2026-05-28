package com.cts.user.dto;

import com.cts.user.model.Role;
import jakarta.validation.constraints.NotNull;

public class UserRoleUpdateRequestDTO {

    @NotNull(message = "Role is required")
    private Role role;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
