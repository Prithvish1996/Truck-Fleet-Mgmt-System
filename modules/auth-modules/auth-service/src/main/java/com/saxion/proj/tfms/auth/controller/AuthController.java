package com.saxion.proj.tfms.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto> > login(@Valid @RequestBody LoginRequestDto request) {
        ApiResponse<LoginResponseDto> response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

}
