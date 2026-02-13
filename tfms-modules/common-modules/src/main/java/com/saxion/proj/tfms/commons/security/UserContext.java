package com.saxion.proj.tfms.commons.security;

public class UserContext {
    private Long userId;
    private String role;
    private String email;
    private boolean valid;

    public UserContext(Long userId, String role, String email, boolean valid) {
        this.userId = userId;
        this.role = role;
        this.email = email;
        this.valid = valid;
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public boolean isValid() { return valid; }
}
