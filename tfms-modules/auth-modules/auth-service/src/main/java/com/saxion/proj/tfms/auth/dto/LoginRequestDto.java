package com.saxion.proj.tfms.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor


@Schema(description = "Login request payload containing user's credentials.")
public class LoginRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema( description = "User's email address", example = "abc@example.com", required = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]+$", 
             message = "Password must contain both letters and numbers")
    @Schema( description = "User's password", example = "Password123", required = true, minLength = 6, pattern = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]+$")
    private String password;
    
}
