package com.saxion.proj.tfms.commons.exception.routing;

/**
 * Base exception for routing-related errors
 */
public class RoutingException extends RuntimeException {
    
    private final String errorCode;
    
    public RoutingException(String message) {
        super(message);
        this.errorCode = "ROUTING_ERROR";
    }
    
    public RoutingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public RoutingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ROUTING_ERROR";
    }
    
    public RoutingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

