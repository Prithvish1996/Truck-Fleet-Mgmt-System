package com.saxion.proj.tfms.planner.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.saxion.proj.tfms.planner.dto.DistanceCalculationResponse;
import com.saxion.proj.tfms.routing.service.facade.RoutingServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for calculating distance and time between two coordinates
 * Uses the routing module's computational facade for distance calculations
 */
@Service
public class DistanceCalculationService {
    
    private static final Logger log = LoggerFactory.getLogger(DistanceCalculationService.class);
    
    private final RoutingServiceFacade routingServiceFacade;
    
    @Autowired
    public DistanceCalculationService(RoutingServiceFacade routingServiceFacade) {
        this.routingServiceFacade = routingServiceFacade;
    }
    
    /**
     * Calculate distance and time between two coordinates using routing module
     * @param originLat Origin latitude
     * @param originLng Origin longitude
     * @param destLat Destination latitude
     * @param destLng Destination longitude
     * @return Distance and time information
     */
    public DistanceCalculationResponse calculateDistance(
            Double originLat, 
            Double originLng, 
            Double destLat, 
            Double destLng) {
        
        try {
            log.info("Calculating distance between ({},{}) and ({},{})",
                    originLat, originLng, destLat, destLng);
            
            // Call routing module's facade for distance calculation
            JsonNode response = routingServiceFacade.calculateDistance(originLat, originLng, destLat, destLng);
            
            return parseResponse(response);
        } catch (Exception e) {
            log.error("Failed to calculate distance: {}", e.getMessage(), e);
            throw new RuntimeException("Distance calculation failed: " + e.getMessage(), e);
        }
    }
    
    private DistanceCalculationResponse parseResponse(JsonNode response) throws Exception {
        if (!response.has("routes") || response.get("routes").size() == 0) {
            throw new RuntimeException("No route found in response");
        }
        
        JsonNode route = response.get("routes").get(0);
        JsonNode summary = route.get("summary");
        
        int distanceMeters = summary.get("lengthInMeters").asInt();
        int timeSeconds = summary.get("travelTimeInSeconds").asInt();
        
        log.info("Distance: {} m, Time: {} s", distanceMeters, timeSeconds);
        
        return DistanceCalculationResponse.from(distanceMeters, timeSeconds);
    }
}
