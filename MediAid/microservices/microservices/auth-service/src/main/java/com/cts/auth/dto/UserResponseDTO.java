package com.cts.auth.dto;

/**
 * Lightweight view of a registered user for admin listing.
 * Deliberately omits password and OTP fields.
 */
public class UserResponseDTO {

    private Long userId;
    private String name;
    private String email;
    private String role;

    public UserResponseDTO() {}

    public UserResponseDTO(Long userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
