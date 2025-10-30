package com.saxion.proj.tfms.commons.exception.routing;

/**
 * Exception thrown when routing request validation fails
 */
public class InvalidRoutingRequestException extends RoutingException {
    
    public InvalidRoutingRequestException(String message) {
        super(message, "INVALID_ROUTING_REQUEST");
    }
    
    public InvalidRoutingRequestException(String message, Throwable cause) {
        super(message, "INVALID_ROUTING_REQUEST", cause);
    }
}

