package com.saxion.proj.tfms.routing.exception;

/**
 * Base exception for routing domain logic failures.
 * Represents recoverable business logic errors that should be handled gracefully.
 */
public class RoutingDomainException extends RuntimeException {
    
    private final String errorCode;
    private final String context;

    public RoutingDomainException(String message, String errorCode) {
        this(message, errorCode, null, null);
    }

    public RoutingDomainException(String message, String errorCode, String context) {
        this(message, errorCode, context, null);
    }

    public RoutingDomainException(String message, String errorCode, Throwable cause) {
        this(message, errorCode, null, cause);
    }

    public RoutingDomainException(String message, String errorCode, String context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getContext() {
        return context;
    }
}
