package com.saxion.proj.tfms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(
        summary = "Health Check",
        description = "Returns the health status of the TFMS application with timestamp"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Application is healthy",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "UP",
                        "service": "tfms-starter",
                        "timestamp": "2025-10-04T10:30:00"
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
        response.put("service", "tfms-starter");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
