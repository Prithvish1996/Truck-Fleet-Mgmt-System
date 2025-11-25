package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saxion.proj.tfms.routing.model.Coordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Clean TomTom Route Calculator
 * Uses TomTom Routing API with computeBestOrder=true for TSP optimization
 */
@Service
public class TomTomRouteCalculator {

    private static final Logger log = LoggerFactory.getLogger(TomTomRouteCalculator.class);
    private static final String TOMTOM_API_BASE_URL = "https://api.tomtom.com/routing/1/calculateRoute";
    
    @Value("${tomtom.api.key:GkSRasdpaBrnBwHN5aO5uhj2hFsR6YHy}")
    private String apiKey = "GkSRasdpaBrnBwHN5aO5uhj2hFsR6YHy";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TomTomRouteCalculator() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }


    /**
     * Get optimized route from TomTom API
     * @return JsonNode containing the response with optimizedWaypoints
     */
    public JsonNode getOptimizedRoute(Coordinates warehouse, List<Coordinates> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            log.warn("No deliveries provided");
            return null;
        }

        try {
            String locations = buildLocationsString(warehouse, deliveries);
            String uri = buildApiUri(locations);

            log.debug("Calling TomTom API for {} deliveries", deliveries.size());
            String response = restTemplate.getForObject(uri, String.class);

            JsonNode jsonResponse = objectMapper.readTree(response);

            if (jsonResponse.has("routes") && jsonResponse.get("routes").size() > 0) {
                JsonNode route = jsonResponse.get("routes").get(0);
                int distanceMeters = route.get("summary").get("lengthInMeters").asInt();
                int timeSeconds = route.get("summary").get("travelTimeInSeconds").asInt();

                log.info("Route calculated: {} km, {} minutes",
                    String.format("%.2f", distanceMeters / 1000.0), timeSeconds / 60);
            }

            return jsonResponse;

        } catch (Exception e) {
            log.error("TomTom API call failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Build locations string for TomTom API
     * Format: warehouse:delivery1:delivery2:...:warehouse
     */
    private String buildLocationsString(Coordinates warehouse, List<Coordinates> deliveries) {
        StringBuilder sb = new StringBuilder();
        sb.append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude());

        for (Coordinates c : deliveries) {
            sb.append(":").append(c.getLatitude()).append(",").append(c.getLongitude());
        }

        sb.append(":").append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude());
        return sb.toString();
    }

    /**
     * Build TomTom API URI with parameters
     */
    private String buildApiUri(String locations) {
        return UriComponentsBuilder.fromUriString(TOMTOM_API_BASE_URL + "/" + locations + "/json")
                .queryParam("key", apiKey)
                .queryParam("computeBestOrder", "true")
                .queryParam("routeRepresentation", "summaryOnly")
                .queryParam("traffic", "true")
                .queryParam("routeType", "fastest")
                .toUriString();
    }

    /**
     * Extract optimized delivery sequence from TomTom response
     * Uses optimizedWaypoints array to reorder deliveries
     *
     * @param response TomTom API JSON response
     * @param originalDeliveries Original delivery coordinates
     * @return Coordinates reordered according to TomTom's optimization
     */
    public List<Coordinates> getOptimizedSequence(JsonNode response, List<Coordinates> originalDeliveries) {
        if (response == null || !response.has("optimizedWaypoints")) {
            log.warn("No optimization data in TomTom response, returning original order");
            return new ArrayList<>(originalDeliveries);
        }

        JsonNode optimizedWaypoints = response.get("optimizedWaypoints");
        if (optimizedWaypoints.size() != originalDeliveries.size()) {
            log.warn("Optimized waypoints count mismatch: {} vs {}",
                optimizedWaypoints.size(), originalDeliveries.size());
            return new ArrayList<>(originalDeliveries);
        }

        // Create array indexed by optimizedIndex
        Coordinates[] orderedDeliveries = new Coordinates[optimizedWaypoints.size()];

        for (JsonNode waypoint : optimizedWaypoints) {
            int providedIndex = waypoint.get("providedIndex").asInt();
            int optimizedIndex = waypoint.get("optimizedIndex").asInt();

            if (providedIndex >= 0 && providedIndex < originalDeliveries.size() &&
                optimizedIndex >= 0 && optimizedIndex < orderedDeliveries.length) {
                orderedDeliveries[optimizedIndex] = originalDeliveries.get(providedIndex);
            }
        }

        // Convert array to list
        List<Coordinates> result = new ArrayList<>();
        for (Coordinates coord : orderedDeliveries) {
            if (coord != null) {
                result.add(coord);
            }
        }

        log.debug("Optimized {} deliveries", result.size());
        return result;
    }
}
