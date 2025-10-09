package com.saxion.proj.tfms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "Test endpoints for TFMS application health and status")
public class TestController {

    @Operation(summary = "Health Check")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "tfms-starter");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * JWT Authentication test endpoint - requires Bearer token
     */
    @Operation(summary = "JWT Authentication Test")
    @GetMapping("/auth")
    public Map<String, Object> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "JWT Authentication successful!");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("authenticated", auth.isAuthenticated());
        response.put("timestamp", LocalDateTime.now());
        
        return response;
    }

    /**
     * Admin only endpoint test
     */
    @Operation(summary = "Admin Access Test")
    @GetMapping("/admin")
    public Map<String, Object> adminTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access successful!");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("timestamp", LocalDateTime.now());
        
        return response;
    }
}
