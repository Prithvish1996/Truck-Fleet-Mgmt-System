package com.saxion.proj.tfms.controller;

import com.saxion.proj.tfms.model.AppStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "TFMS API", description = "Main API endpoints for Truck Fleet Management System")
public class TfmsApiController {

    @Operation(
        summary = "Get application information",
        description = "Returns detailed information about the TFMS application including version, status, and configuration"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved application information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppStatus.class)
            )
        )
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
        description = "Returns the current system status with optional detailed information",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "System status retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "HEALTHY",
                        "uptime": "2h 30m",
                        "memory": "512MB",
                        "cpu": "15%",
                        "database": "CONNECTED"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token required",
            content = @Content
        )
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
        description = "Updates system configuration parameters (Admin only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuration updated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "message": "Configuration updated successfully",
                        "timestamp": "2025-10-04T10:30:00",
                        "updatedFields": ["maxConnections", "timeout"]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid configuration parameters",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin access required",
            content = @Content
        )
    })
    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfiguration(
            @Parameter(description = "Configuration parameters to update", required = true)
            @RequestBody Map<String, Object> config) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Configuration updated successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("updatedFields", config.keySet());
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get API metrics",
        description = "Returns API usage metrics and statistics"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Metrics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "totalRequests": 1250,
                        "requestsPerMinute": 25,
                        "averageResponseTime": "150ms",
                        "errorRate": "0.5%",
                        "activeUsers": 12
                    }
                    """
                )
            )
        )
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
}
