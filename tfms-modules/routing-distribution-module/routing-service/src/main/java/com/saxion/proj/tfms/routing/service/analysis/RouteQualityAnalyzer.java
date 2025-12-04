package com.saxion.proj.tfms.routing.service.analysis;

import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.model.TruckRouteInfo;
import org.springframework.stereotype.Component;

/**
 * Analyzes route quality metrics.
 * Provides insights into route optimization effectiveness.
 */
@Component
public class RouteQualityAnalyzer {

    /**
     * Calculate average distance per stop.
     */
    public double calculateAverageDistancePerStop(TruckRouteInfo route) {
        if (route.getRouteStops().isEmpty()) {
            return 0.0;
        }
        return (double) route.getTotalDistance() / route.getRouteStops().size();
    }

    /**
     * Calculate average time per stop.
     */
    public double calculateAverageTimePerStop(TruckRouteInfo route) {
        if (route.getRouteStops().isEmpty()) {
            return 0.0;
        }
        return (double) route.getTotalTransportTime() / route.getRouteStops().size();
    }

    /**
     * Get stop count.
     */
    public int getStopCount(TruckRouteInfo route) {
        return route.getRouteStops().size();
    }

    /**
     * Get customer delivery count (excludes depot/warehouse stops).
     */
    public long getCustomerDeliveryCount(TruckRouteInfo route) {
        return route.getRouteStops().stream()
                .filter(stop -> !isDepotOrWarehouseStop(stop))
                .count();
    }

    /**
     * Calculate route efficiency score (0-100).
     * Higher is better.
     */
    public int calculateEfficiencyScore(TruckRouteInfo route) {
        long customerDeliveries = getCustomerDeliveryCount(route);
        int totalStops = getStopCount(route);

        if (totalStops == 0) {
            return 0;
        }

        // Efficiency is based on ratio of customer deliveries to total stops
        // Normalized to 0-100 scale
        return (int) ((customerDeliveries * 100) / totalStops);
    }

    private boolean isDepotOrWarehouseStop(Stop stop) {
        return stop.getStopType().toString().contains("DEPOT") || 
               stop.getStopType().toString().contains("WAREHOUSE");
    }
}
