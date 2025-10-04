package com.saxion.proj.tfms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "Application health monitoring endpoints")
public class HealthController {

    @Operation(
        summary = "Application Health Status",
        description = "Returns the overall health status of the TFMS application"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Application is running successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "UP",
                        "service": "TFMS",
                        "timestamp": "2025-10-04T10:30:00",
                        "message": "Service is running"
                    }
                    """
                )
            )
        )
    })
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
