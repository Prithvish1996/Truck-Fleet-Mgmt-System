package com.saxion.proj.tfms.controller;

import com.saxion.proj.tfms.model.AppStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "TFMS System Management", description = "Main API endpoints for Truck Fleet Management System monitoring and configuration")
public class TfmsApiController {

    @Operation(
            summary = "Get application information",
            description = "Retrieves basic information about the TFMS application including status, version, and timestamp"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application information retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AppStatus.class)))
    })
    @GetMapping("/info")
    public ResponseEntity<AppStatus> getApplicationInfo() {
        AppStatus status = new AppStatus(
            "UP",
            "TFMS - Truck Fleet Management System",
            LocalDateTime.now(),
            "Application is running successfully",
            "1.0.0-dev"
        );
        return ResponseEntity.ok(status);
    }

    @Operation(
            summary = "Get system status",
            description = "Retrieves the current system health status with optional detailed metrics"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System status retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus(
            @Parameter(description = "Include detailed system metrics", example = "true")
            @RequestParam(value = "detailed", defaultValue = "false") boolean detailed) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "HEALTHY");
        response.put("timestamp", LocalDateTime.now());
        
        if (detailed) {
            response.put("uptime", "2h 30m");
            response.put("memory", "512MB");
            response.put("cpu", "15%");
            response.put("database", "CONNECTED");
            response.put("activeConnections", 5);
        }
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update system configuration",
            description = "Updates system configuration parameters dynamically"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Invalid configuration data")
    })
    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfiguration(
            @Parameter(description = "Configuration parameters to update")
            @RequestBody Map<String, Object> config) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Configuration updated successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("updatedFields", config.keySet());
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get API metrics",
            description = "Retrieves performance metrics and usage statistics for the API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", 1250);
        metrics.put("requestsPerMinute", 25);
        metrics.put("averageResponseTime", "150ms");
        metrics.put("errorRate", "0.5%");
        metrics.put("activeUsers", 12);
        metrics.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(metrics);
    }

    @Operation(
            summary = "Health check endpoint",
            description = "Simple health check endpoint to verify if the service is running"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "TFMS");
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
