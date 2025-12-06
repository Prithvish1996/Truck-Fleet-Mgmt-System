package com.saxion.proj.tfms.routing.service.computation.strategy;

import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;
import java.util.List;

/**
 * Strategy pattern for different route optimization algorithms.
 * Allows pluggable routing providers (TomTom, Google Maps, etc.)
 */
public interface RouteOptimizationStrategy {

    /**
     * Calculate optimized route for given coordinates.
     * @param coordinates Contains depot, warehouse, and delivery locations
     * @return Ordered list of stops for the optimal route
     * @throws com.saxion.proj.tfms.routing.exception.RouteOptimizationException if optimization fails
     */
    List<Stop> optimizeRoute(RouteCoordinatesGroup coordinates);

    /**
     * Get the name/identifier of this strategy.
     */
    String getStrategyName();

    /**
     * Check if this strategy is available/enabled.
     */
    boolean isAvailable();
}
