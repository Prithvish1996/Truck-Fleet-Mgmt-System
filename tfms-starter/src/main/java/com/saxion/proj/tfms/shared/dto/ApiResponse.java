package com.saxion.proj.tfms.shared.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic API response wrapper.
 * This class encapsulates the standard structure of an API response,
 * including success status, message, data, error code, and timestamp.
 * @param <T> the type of the data included in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Builder.Default
    private boolean success = false;
    
    private String message;
    
    private T data;
    
    private String errorCode;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Static factory methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode("GENERAL_ERROR")
                .build();
    }

}
