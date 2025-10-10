package com.saxion.proj.tfms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "Application health monitoring endpoints")
public class HealthController {

    @Operation(summary = "Application Health Status")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "TFMS");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Service is running");
        return response;
    }
}
