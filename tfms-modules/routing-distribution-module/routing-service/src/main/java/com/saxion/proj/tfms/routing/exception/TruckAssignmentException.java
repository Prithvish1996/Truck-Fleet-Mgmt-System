package com.saxion.proj.tfms.routing.exception;

/**
 * Thrown when truck assignment fails for business reasons (e.g., insufficient capacity, no available trucks).
 */
public class TruckAssignmentException extends RoutingDomainException {
    
    public TruckAssignmentException(String message) {
        super(message, "TRUCK_ASSIGNMENT_ERROR");
    }

    public TruckAssignmentException(String message, String context) {
        super(message, "TRUCK_ASSIGNMENT_ERROR", context);
    }

    public TruckAssignmentException(String message, Throwable cause) {
        super(message, "TRUCK_ASSIGNMENT_ERROR", cause);
    }
}
