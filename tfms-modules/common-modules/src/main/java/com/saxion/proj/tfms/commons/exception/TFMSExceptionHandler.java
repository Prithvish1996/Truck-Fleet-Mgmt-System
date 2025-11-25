package com.saxion.proj.tfms.commons.exception;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.exception.auth.AccountLockedException;
import com.saxion.proj.tfms.commons.exception.auth.AuthenticationException;
import com.saxion.proj.tfms.commons.exception.auth.InvalidCredentialsException;
import com.saxion.proj.tfms.commons.exception.auth.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



@Slf4j
@ControllerAdvice(basePackages = "com.saxion.proj.tfms")
public class TFMSExceptionHandler {

    // ========== Validation Exceptions ==========
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .data(errors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(e.getMessage(), "INVALID_INPUT");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalState(IllegalStateException e) {
        log.error("Illegal state: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(e.getMessage(), "ILLEGAL_STATE");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<String>> handleNullPointer(NullPointerException e) {
        log.error("Null pointer exception: ", e);
        
        ApiResponse<String> response = ApiResponse.error(
                "A required value was not provided", 
                "NULL_VALUE");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ========== Custom Authentication Exceptions ==========

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication error: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidCredentials(InvalidCredentialsException e) {
        // Log detailed error for administrators (not exposed to client)
        log.warn("Authentication failed - Invalid credentials: {}", e.getMessage());
        
        // Generic response - don't reveal specific failure reason
        ApiResponse<String> response = ApiResponse.error("Authentication failed", "INVALID_CREDENTIALS");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFound(UserNotFoundException e) {
        // Log detailed error for administrators (not exposed to client)
        log.warn("Authentication failed - User not found: {}", e.getMessage());
        
        // Generic response - same as invalid credentials to prevent user enumeration
        ApiResponse<String> response = ApiResponse.error("Authentication failed", "INVALID_CREDENTIALS");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccountLocked(AccountLockedException e) {
        log.error("Account locked: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ========== Routing Module Exceptions ==========

    /**
     * Handle routing-related exceptions using reflection to avoid hard dependency.
     * This allows the common module to handle routing exceptions without importing routing-service module.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRoutingExceptions(RuntimeException e) {
        String className = e.getClass().getSimpleName();

        // Handle VRP Optimization Exception
        if (className.equals("VrpOptimizationException")) {
            log.error("VRP optimization failed: {}", e.getMessage(), e);
            String errorCode = getErrorCodeViaReflection(e);
            ApiResponse<String> response = ApiResponse.error(e.getMessage(), errorCode != null ? errorCode : "VRP_OPTIMIZATION_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Handle Invalid Routing Request Exception
        if (className.equals("InvalidRoutingRequestException")) {
            log.error("Invalid routing request: {}", e.getMessage());
            String errorCode = getErrorCodeViaReflection(e);
            ApiResponse<String> response = ApiResponse.error(e.getMessage(), errorCode != null ? errorCode : "INVALID_ROUTING_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Handle Routing Service Exception
        if (className.equals("RoutingServiceException")) {
            log.error("Routing service error: {}", e.getMessage(), e);
            String errorCode = getErrorCodeViaReflection(e);
            ApiResponse<String> response = ApiResponse.error(e.getMessage(), errorCode != null ? errorCode : "ROUTING_SERVICE_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Handle Base Routing Exception
        if (className.equals("RoutingException")) {
            log.error("Routing error: {}", e.getMessage(), e);
            String errorCode = getErrorCodeViaReflection(e);
            ApiResponse<String> response = ApiResponse.error(e.getMessage(), errorCode != null ? errorCode : "ROUTING_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // If not a routing exception, pass to generic handler
        throw e;
    }

    /**
     * Helper method to extract error code from exceptions using reflection
     */
    private String getErrorCodeViaReflection(Exception e) {
        try {
            java.lang.reflect.Method getErrorCode = e.getClass().getMethod("getErrorCode");
            Object result = getErrorCode.invoke(e);
            return result != null ? result.toString() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    // ========== I/O and Threading Exceptions ==========

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<String>> handleIOException(IOException e) {
        log.error("I/O error: {}", e.getMessage(), e);

        ApiResponse<String> response = ApiResponse.error(
                "An I/O error occurred while processing the request",
                "IO_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ApiResponse<String>> handleInterruptedException(InterruptedException e) {
        log.error("Thread interrupted: {}", e.getMessage(), e);
        Thread.currentThread().interrupt(); // Restore interrupt status

        ApiResponse<String> response = ApiResponse.error(
                "The operation was interrupted",
                "OPERATION_INTERRUPTED");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ========== Generic Exception Handler ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        
        ApiResponse<String> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later", 
                "INTERNAL_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

