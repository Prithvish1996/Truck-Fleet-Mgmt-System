package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metrics for a single truck route including distance, time, and delivery count
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckRouteMetrics {
    private int truckIndex;           // Truck/shift index
    private int distanceInMeters;     // Total distance in meters
    private int timeInSeconds;        // Total time in seconds
    private int deliveryCount;        // Number of deliveries
    private double averageSpeed;      // Average speed in km/h

    /**
     * Calculate average speed from distance and time
     */
    public void calculateAverageSpeed() {
        if (timeInSeconds > 0) {
            double distanceKm = distanceInMeters / 1000.0;
            double timeHours = timeInSeconds / 3600.0;
            this.averageSpeed = distanceKm / timeHours;
        } else {
            this.averageSpeed = 0.0;
        }
    }

    /**
     * Constructor without average speed (will be calculated)
     */
    public TruckRouteMetrics(int truckIndex, int distanceInMeters, int timeInSeconds, int deliveryCount) {
        this.truckIndex = truckIndex;
        this.distanceInMeters = distanceInMeters;
        this.timeInSeconds = timeInSeconds;
        this.deliveryCount = deliveryCount;
        calculateAverageSpeed();
    }
}
