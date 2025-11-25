package com.saxion.proj.tfms.commons.exception.routing;

/**
 * Exception thrown when VRP optimization fails
 */
public class VrpOptimizationException extends RoutingException {
    
    public VrpOptimizationException(String message) {
        super(message, "VRP_OPTIMIZATION_FAILED");
    }
    
    public VrpOptimizationException(String message, Throwable cause) {
        super(message, "VRP_OPTIMIZATION_FAILED", cause);
    }
}

