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
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the TFMS application.
 * This class captures various exceptions thrown during request processing
 * and converts them into standardized API responses.
 * 
 * Note: This handler only includes basic validation exceptions.
 * For more advanced web exceptions, add spring-boot-starter-web dependency.
 * 
 * Author: Prithvish.Chakraborty@quest.com
 * Date: 2023-05-21
 */

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
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        
        ApiResponse<String> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later", 
                "INTERNAL_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}