package com.saxion.proj.tfms.auth.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private Map<String, Map<String, Object>> users = new HashMap<>();
    private Map<String, String> activeSessions = new HashMap<>();

    public AuthController() {
        // Initialize with sample / dummy static users
        Map<String, Object> admin = new HashMap<>();
        admin.put("username", "admin");
        admin.put("email", "admin@tfms.com");
        admin.put("role", "ADMIN");
        admin.put("createdAt", LocalDateTime.now());
        users.put("admin", admin);

        Map<String, Object> driver = new HashMap<>();
        driver.put("username", "driver1");
        driver.put("email", "driver1@tfms.com");
        driver.put("role", "DRIVER");
        driver.put("createdAt", LocalDateTime.now());
        users.put("driver1", driver);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> response = new HashMap<>();
        if (users.containsKey(username)) {
            String token = UUID.randomUUID().toString();
            activeSessions.put(token, username);
            
            response.put("token", token);
            response.put("user", users.get(username));
            response.put("message", "Login successful");
            response.put("status", "SUCCESS");
        } else {
            response.put("message", "Invalid credentials");
            response.put("status", "ERROR");
        }
        return response;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader("Authorization") String token) {
        activeSessions.remove(token);
        return Map.of("message", "Logout successful", "status", "SUCCESS");
    }

    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String token) {
        String username = activeSessions.get(token);
        if (username != null) {
            return Map.of(
                "valid", true,
                "user", users.get(username)
            );
        }
        return Map.of("valid", false, "message", "Invalid token");
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
