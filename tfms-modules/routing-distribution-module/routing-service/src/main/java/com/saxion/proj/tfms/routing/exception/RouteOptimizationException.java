package com.saxion.proj.tfms.routing.exception;

/**
 * Thrown when route optimization fails (e.g., TomTom API error, invalid coordinates).
 */
public class RouteOptimizationException extends RoutingDomainException {
    
    public RouteOptimizationException(String message) {
        super(message, "ROUTE_OPTIMIZATION_ERROR");
    }

    public RouteOptimizationException(String message, String context) {
        super(message, "ROUTE_OPTIMIZATION_ERROR", context);
    }

    public RouteOptimizationException(String message, Throwable cause) {
        super(message, "ROUTE_OPTIMIZATION_ERROR", cause);
    }
}
