package com.saxion.proj.tfms.commons.exception.routing;

/**
 * Exception thrown when routing service encounters an internal error
 */
public class RoutingServiceException extends RoutingException {

    public RoutingServiceException(String message) {
        super(message, "ROUTING_SERVICE_ERROR");
    }
    
    public RoutingServiceException(String message, Throwable cause) {
        super(message, "ROUTING_SERVICE_ERROR", cause);
    }
}
