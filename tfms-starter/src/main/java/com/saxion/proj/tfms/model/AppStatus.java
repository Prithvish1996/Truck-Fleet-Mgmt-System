package com.saxion.proj.tfms.model;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Schema(description = "Application status information")
public class AppStatus {

    @Schema(description = "Current status of the application", example = "UP", allowableValues = {"UP", "DOWN", "MAINTENANCE"})
    private String status;

    @Schema(description = "Name of the service", example = "TFMS")
    private String service;

    @Schema(description = "Current timestamp when status was checked", example = "2025-10-04T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Additional status message", example = "Service is running")
    private String message;

    @Schema(description = "Application version", example = "1.0.0-dev")
    private String version;

    // Constructors
    public AppStatus() {}

    public AppStatus(String status, String service, LocalDateTime timestamp, String message, String version) {
        this.status = status;
        this.service = service;
        this.timestamp = timestamp;
        this.message = message;
        this.version = version;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
