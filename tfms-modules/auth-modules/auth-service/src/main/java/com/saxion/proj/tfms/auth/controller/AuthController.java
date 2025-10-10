package com.saxion.proj.tfms.auth.controller;

import com.saxion.proj.tfms.commons.service.TokenBlacklistService;
import com.saxion.proj.tfms.commons.swagger.SwaggerAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.saxion.proj.tfms.auth.dto.LoginRequestDto;
import com.saxion.proj.tfms.auth.dto.LoginResponseDto;
import com.saxion.proj.tfms.auth.service.AuthService;
import com.saxion.proj.tfms.commons.dto.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @SwaggerAnnotations.StandardApiOperation(
            summary = "User login",
            description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto> > login(@Valid @RequestBody LoginRequestDto request) {
        ApiResponse<LoginResponseDto> response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @SwaggerAnnotations.StandardApiOperation(
            summary = "User logout",
            description = "Invalidate user session or token"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        tokenBlacklistService.blackListToken(token);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

}
