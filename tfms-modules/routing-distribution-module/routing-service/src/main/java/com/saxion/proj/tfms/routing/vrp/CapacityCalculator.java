package com.saxion.proj.tfms.routing.vrp;

import com.saxion.proj.tfms.routing.dto.TruckInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Calculates capacity-related metrics for truck fleet optimization.
 * Single Responsibility Principle: Handles only capacity calculations
 */
@Component
public class CapacityCalculator {

    public int calculateTrucksNeeded(double totalVolume, List<TruckInfo> trucks) {
        if (totalVolume <= 0) {
            return 0;
        }
        
        double maxCapacity = trucks.stream()
                .mapToDouble(TruckInfo::getVolume)
                .max()
                .orElse(100.0);
        
        int trucksNeeded = (int) Math.ceil(totalVolume / maxCapacity);
        return Math.max(1, trucksNeeded);
    }

    public double calculateUtilization(double totalParcelVolume, double totalTruckCapacity) {
        if (totalTruckCapacity <= 0) {
            return 0.0;
        }
        return totalParcelVolume / totalTruckCapacity;
    }

    public double calculateCapacityWeight(double utilization) {
        if (utilization > 0.9) {
            return 0.5;
        } else if (utilization > 0.7) {
            return 0.7;
        } else if (utilization > 0.5) {
            return 1.0;
        } else if (utilization > 0.3) {
            return 1.3;
        } else {
            return 1.5;
        }
    }

    public double calculateCapacityFactor(double utilization) {
        if (utilization > 0.8) {
            return 0.7;
        } else if (utilization > 0.6) {
            return 0.85;
        } else {
            return 1.2;
        }
    }
}
