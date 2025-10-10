package com.saxion.proj.tfms.config.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for security-conscious error messages
 * Implements generic error responses to prevent information disclosure
 */
@RestControllerAdvice
public class SecurityErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityErrorHandler.class);
    
 
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        logger.warn("Authentication failed for request: {} - Reason: {}", 
                   request.getDescription(false), ex.getMessage());
        
        Map<String, Object> response = createGenericErrorResponse(
            "Authentication failed", 
            "Invalid credentials"
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle general security exceptions
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(
            SecurityException ex, WebRequest request) {
        
        logger.warn("Security exception for request: {} - Reason: {}", 
                   request.getDescription(false), ex.getMessage());
        
        Map<String, Object> response = createGenericErrorResponse(
            "Access denied", 
            "Insufficient permissions"
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handle illegal argument exceptions (often from validation)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Invalid request for: {} - Reason: {}", 
                   request.getDescription(false), ex.getMessage());
        
        Map<String, Object> response = createGenericErrorResponse(
            "Invalid request", 
            "Please check your input and try again"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Fallback for any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex, WebRequest request) {
        
        // Log full stack trace for debugging (not exposed to client)
        logger.error("Unexpected error for request: {} - Error: {}", 
                    request.getDescription(false), ex.getMessage(), ex);
        
        Map<String, Object> response = createGenericErrorResponse(
            "Service temporarily unavailable", 
            "Please try again later"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Creates a generic error response structure
     */
    private Map<String, Object> createGenericErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("error", error);
        response.put("message", message);
        // Don't include: stack traces, specific reasons, system info, etc.
        return response;
    }
}
