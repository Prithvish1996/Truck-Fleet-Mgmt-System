package com.saxion.proj.tfms.routing.service.computation.helper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.saxion.proj.tfms.routing.model.Coordinates;
import java.util.List;

/**
 * Contract for external routing API clients.
 * Decouples from specific provider (TomTom, Google Maps, etc.)
 */
public interface RoutingApiClient {

    /**
     * Calculate optimized route.
     * @return Response containing optimized waypoint order
     * @throws com.saxion.proj.tfms.routing.exception.RoutingProviderException on API failure
     */
    JsonNode calculateOptimizedRoute(Coordinates warehouse, List<Coordinates> deliveries);

    /**
     * Extract optimized delivery sequence from API response.
     */
    List<Coordinates> extractOptimizedSequence(JsonNode response, List<Coordinates> originalDeliveries);

    /**
     * Get client name.
     */
    String getClientName();

    /**
     * Check if client is properly configured and available.
     */
    boolean isAvailable();
}
