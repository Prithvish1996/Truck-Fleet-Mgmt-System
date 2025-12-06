package com.saxion.proj.tfms.routing.service.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.saxion.proj.tfms.routing.model.Coordinates;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;
import com.saxion.proj.tfms.routing.service.OptimizeRouting;
import com.saxion.proj.tfms.routing.service.computation.helper.client.RoutingApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade service providing simplified access to routing optimization.
 * Hides complexity of the internal service composition.
 */
@Service
public class RoutingServiceFacade {

    private static final Logger log = LoggerFactory.getLogger(RoutingServiceFacade.class);
    
    private final OptimizeRouting optimizer;
    private final RoutingApiClient routingApiClient;

    @Autowired
    public RoutingServiceFacade(OptimizeRouting optimizer, RoutingApiClient routingApiClient) {
        this.optimizer = optimizer;
        this.routingApiClient = routingApiClient;
    }

    /**
     * Optimize routes for given VRP request.
     * @param request Vehicle routing problem specification
     * @return Optimized routes per warehouse
     */
    public VRPResponse optimizeRoutes(VRPRequest request) {
        return optimizer.optimize(request);
    }
    
    /**
     * Calculate distance and time between two coordinates.
     * Uses TomTom API via the routing computational module.
     * 
     * @param originLat Origin latitude
     * @param originLng Origin longitude
     * @param destLat Destination latitude
     * @param destLng Destination longitude
     * @return JsonNode containing distance (lengthInMeters) and time (travelTimeInSeconds)
     */
    public JsonNode calculateDistance(Double originLat, Double originLng, 
                                      Double destLat, Double destLng) {
        try {
            log.info("Calculating distance between ({},{}) and ({},{})",
                    originLat, originLng, destLat, destLng);
            
            if (!routingApiClient.isAvailable()) {
                throw new RuntimeException("Routing API client is not available");
            }
            
            // Create warehouse (origin) and single delivery (destination)
            Coordinates warehouse = new Coordinates(originLat, originLng);
            List<Coordinates> deliveries = new ArrayList<>();
            deliveries.add(new Coordinates(destLat, destLng));
            
            // Call TomTom API via routing module's client
            JsonNode response = routingApiClient.calculateOptimizedRoute(warehouse, deliveries);
            
            log.info("Distance calculation completed successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to calculate distance: {}", e.getMessage(), e);
            throw new RuntimeException("Distance calculation failed: " + e.getMessage(), e);
        }
    }
}
