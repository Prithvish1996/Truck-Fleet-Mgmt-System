package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.dto.DistanceCalculationRequest;
import com.saxion.proj.tfms.planner.dto.DistanceCalculationResponse;
import com.saxion.proj.tfms.planner.services.DistanceCalculationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for distance and time calculation between coordinates
 * Uses TomTom API for route calculation
 * Only accessible to authenticated planner users
 */
@RestController
@RequestMapping("/api/planner/distance")
public class DistanceCalculationController {
    
    @Autowired
    private DistanceCalculationService distanceCalculationService;
    
    /**
     * Calculate distance and time between two coordinates
     * @param request Request containing origin and destination coordinates
     * @param user Current authenticated user
     * @return Distance and time information
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<DistanceCalculationResponse>> calculateDistance(
            @Valid @RequestBody DistanceCalculationRequest request,
            @CurrentUser UserContext user) {
        
        try {
            if (!request.isValid()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Invalid coordinates", "INVALID_COORDINATES")
                );
            }
            
            DistanceCalculationResponse response = distanceCalculationService.calculateDistance(
                    request.getOriginLatitude(),
                    request.getOriginLongitude(),
                    request.getDestinationLatitude(),
                    request.getDestinationLongitude()
            );
            
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Distance calculated successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to calculate distance: " + e.getMessage(), 
                            "DISTANCE_CALCULATION_ERROR")
            );
        }
    }
    
    /**
     * Calculate distance and time using query parameters
     * @param originLat Origin latitude
     * @param originLng Origin longitude
     * @param destLat Destination latitude
     * @param destLng Destination longitude
     * @param user Current authenticated user
     * @return Distance and time information
     */
    @GetMapping("/calculate")
    public ResponseEntity<ApiResponse<DistanceCalculationResponse>> calculateDistanceGet(
            @RequestParam(name = "originLat") Double originLat,
            @RequestParam(name = "originLng") Double originLng,
            @RequestParam(name = "destLat") Double destLat,
            @RequestParam(name = "destLng") Double destLng,
            @CurrentUser UserContext user) {
        
        try {
            DistanceCalculationRequest request = DistanceCalculationRequest.builder()
                    .originLatitude(originLat)
                    .originLongitude(originLng)
                    .destinationLatitude(destLat)
                    .destinationLongitude(destLng)
                    .build();
            
            if (!request.isValid()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Invalid coordinates", "INVALID_COORDINATES")
                );
            }
            
            DistanceCalculationResponse response = distanceCalculationService.calculateDistance(
                    originLat, originLng, destLat, destLng
            );
            
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Distance calculated successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to calculate distance: " + e.getMessage(), 
                            "DISTANCE_CALCULATION_ERROR")
            );
        }
    }
}
