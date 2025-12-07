package com.saxion.proj.tfms.routing.exception;

/**
 * Thrown when external routing API calls fail (e.g., TomTom API, timeout, network error).
 */
public class RoutingProviderException extends RoutingDomainException {
    
    public RoutingProviderException(String message, String providerName) {
        super(message, "ROUTING_PROVIDER_ERROR", providerName);
    }

    public RoutingProviderException(String message, String providerName, Throwable cause) {
        super(message, "ROUTING_PROVIDER_ERROR", providerName, cause);
    }

    public String getProviderName() {
        return getContext();
    }
}
