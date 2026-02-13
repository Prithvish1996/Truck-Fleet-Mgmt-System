package com.saxion.proj.tfms.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Keep your existing structure, just update field names for JWT:
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Login response payload containing JWT token and user details.")
public class LoginResponseDto {
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    @Schema(description = "Refresh token", example = "dGhpc2lzYXJlZnJlc2h0b2tlbg==")
    private String refreshToken;
    @Schema(description = "Type of the token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    @Schema(description = "Token expiration time in seconds", example = "3600")
    private Long expiresIn;
    @Schema(description = "Username of the authenticated user", example = "john_doe")
    private String username;
    @Schema(description = "Type of the user", example = "admin")
    private String userType;
    @Schema(description = "Email of the authenticated user", example = "abc@example.com")
    private String email;
    @Schema(description = "Indicates if the login was successful", example = "true")
    private boolean success;
    @Schema(description = "Additional message", example = "Login successful")
    private String message;
}
