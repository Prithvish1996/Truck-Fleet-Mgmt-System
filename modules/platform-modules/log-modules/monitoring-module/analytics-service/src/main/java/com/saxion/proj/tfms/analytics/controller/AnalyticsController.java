package com.saxion.proj.tfms.analytics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "analytics-service");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Analytics Service");
        response.put("version", "1.0.0-SNAPSHOT");
        response.put("description", "Truck Fleet Management Analytics Service");
        response.put("endpoints", new String[]{"/api/analytics/health", "/api/analytics/info"});
        return response;
    }
}
