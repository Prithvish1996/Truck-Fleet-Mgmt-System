package com.saxion.proj.tfms.auth.dto;

import com.saxion.proj.tfms.commons.model.UserDao;

import java.time.ZonedDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserDto {
    private Long userId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema( description = "User's email address", example = "abc@example.com", required = true)
    private String email;
    private String username;
    private UserDao.UserType userType;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
