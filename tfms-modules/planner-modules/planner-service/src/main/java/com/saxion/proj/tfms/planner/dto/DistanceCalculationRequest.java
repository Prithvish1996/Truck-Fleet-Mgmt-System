package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for distance calculation between two coordinates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceCalculationRequest {
    
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    
    public boolean isValid() {
        return originLatitude != null && originLongitude != null &&
               destinationLatitude != null && destinationLongitude != null &&
               !originLatitude.isNaN() && !originLongitude.isNaN() &&
               !destinationLatitude.isNaN() && !destinationLongitude.isNaN();
    }
}
