package com.saxion.proj.tfms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Keep your existing structure, just update field names for JWT:
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;     // JWT token
    private String refreshToken;    // Optional refresh token
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;        // Token expiry in milliseconds
    private String username;       // User's email/username
    private String userType;       // Changed from 'role' to match UserDao.UserType
    private String email;          // Add email field
    private boolean success;       // Keep for response status
    private String message;        // Keep for response message
}
