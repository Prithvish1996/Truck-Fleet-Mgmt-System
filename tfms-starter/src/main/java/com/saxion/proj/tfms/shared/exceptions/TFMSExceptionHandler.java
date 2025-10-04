package com.saxion.proj.tfms.shared.exceptions;

import com.saxion.proj.tfms.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the TFMS application.
 * This class captures various exceptions thrown during request processing
 * and converts them into standardized API responses.
 * 
 * Author: Prithvish.Chakraborty@quest.com
 * Date: 2023-05-21
 */

@Slf4j
@ControllerAdvice
public class TFMSExceptionHandler {

   
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("Validation error on {}: {}", request.getRequestURI(), e.getMessage());
        
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


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<String>> handleMissingParameter(
            MissingServletRequestParameterException e) {
        log.error("Missing parameter: {}", e.getMessage());
        
        String message = String.format("Required parameter '%s' is missing", e.getParameterName());
        ApiResponse<String> response = ApiResponse.error(message, "MISSING_PARAMETER");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch: {}", e.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'", 
                e.getValue(), e.getName());
        ApiResponse<String> response = ApiResponse.error(message, "INVALID_PARAMETER_TYPE");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

   
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e) {
        log.error("Method not supported: {}", e.getMessage());
        
        String message = String.format("HTTP method '%s' is not supported for this endpoint", 
                e.getMethod());
        ApiResponse<String> response = ApiResponse.error(message, "METHOD_NOT_SUPPORTED");
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e) {
        log.error("Media type not supported: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(
                "Unsupported media type. Please check Content-Type header", 
                "UNSUPPORTED_MEDIA_TYPE");
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleMessageNotReadable(
            HttpMessageNotReadableException e) {
        log.error("Message not readable: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(
                "Invalid JSON format or malformed request body", 
                "MALFORMED_JSON");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

   
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDenied(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(
                "Access denied. Insufficient permissions", 
                "ACCESS_DENIED");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
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

    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFound(NoHandlerFoundException e) {
        log.error("Endpoint not found: {}", e.getRequestURL());
        
        ApiResponse<String> response = ApiResponse.error(
                "The requested endpoint does not exist", 
                "ENDPOINT_NOT_FOUND");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

   
    @ExceptionHandler(MessageDeliveryException.class)
    public ResponseEntity<ApiResponse<String>> handleMessageDelivery(
            MessageDeliveryException e) {
        log.error("Message delivery failed: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(
                "Failed to deliver WebSocket message", 
                "WEBSOCKET_DELIVERY_FAILED");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ========== Generic Exception Handler (Catch-all) ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(
            Exception e, HttpServletRequest request) {
        log.error("Unexpected error on {}: ", request.getRequestURI(), e);
        
        ApiResponse<String> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later", 
                "INTERNAL_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}