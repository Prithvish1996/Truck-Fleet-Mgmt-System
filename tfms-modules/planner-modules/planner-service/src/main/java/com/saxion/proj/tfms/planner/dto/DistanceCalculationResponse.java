package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for distance calculation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceCalculationResponse {
    
    private int distanceInMeters;
    private double distanceInKilometers;
    private int timeInSeconds;
    private int timeInMinutes;
    private String formattedDistance;
    private String formattedTime;
    
    public static DistanceCalculationResponse from(int distanceMeters, int timeSeconds) {
        double distanceKm = distanceMeters / 1000.0;
        int timeMinutes = timeSeconds / 60;
        
        return DistanceCalculationResponse.builder()
                .distanceInMeters(distanceMeters)
                .distanceInKilometers(Math.round(distanceKm * 100.0) / 100.0)
                .timeInSeconds(timeSeconds)
                .timeInMinutes(timeMinutes)
                .formattedDistance(String.format("%.2f km", distanceKm))
                .formattedTime(formatTime(timeMinutes))
                .build();
    }
    
    private static String formatTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        
        if (hours > 0) {
            return String.format("%d h %d min", hours, mins);
        } else {
            return String.format("%d min", mins);
        }
    }
}
