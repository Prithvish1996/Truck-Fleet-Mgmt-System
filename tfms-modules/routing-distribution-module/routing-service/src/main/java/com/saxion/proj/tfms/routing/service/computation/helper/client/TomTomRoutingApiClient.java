package com.saxion.proj.tfms.routing.service.computation.helper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saxion.proj.tfms.routing.exception.RoutingProviderException;
import com.saxion.proj.tfms.routing.model.Coordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * TomTom routing API client implementation.
 * Handles TomTom-specific API interaction details.
 */
@Component
public class TomTomRoutingApiClient implements RoutingApiClient {

    private static final Logger log = LoggerFactory.getLogger(TomTomRoutingApiClient.class);
    private static final String CLIENT_NAME = "TomTom";
    private static final String TOMTOM_API_BASE_URL = "https://api.tomtom.com/routing/1/calculateRoute";

    @Value("${tomtom.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public TomTomRoutingApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode calculateOptimizedRoute(Coordinates warehouse, List<Coordinates> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            throw new RoutingProviderException("No deliveries provided", CLIENT_NAME);
        }

        try {
            String locations = buildLocationsString(warehouse, deliveries);
            String uri = buildApiUri(locations);

            log.debug("Calling TomTom API for {} deliveries", deliveries.size());
            String response = restTemplate.getForObject(uri, String.class);

            JsonNode jsonResponse = objectMapper.readTree(response);
            logRouteMetrics(jsonResponse);

            return jsonResponse;
        } catch (Exception e) {
            log.error("TomTom API call failed: {}", e.getMessage(), e);
            throw new RoutingProviderException("TomTom API call failed", CLIENT_NAME, e);
        }
    }

    @Override
    public List<Coordinates> extractOptimizedSequence(JsonNode response, List<Coordinates> originalDeliveries) {
        if (response == null || !response.has("optimizedWaypoints")) {
            log.warn("No optimization data in TomTom response");
            return new ArrayList<>(originalDeliveries);
        }

        try {
            JsonNode optimizedWaypoints = response.get("optimizedWaypoints");
            validateWaypointCount(optimizedWaypoints, originalDeliveries);

            return reorderDeliveries(optimizedWaypoints, originalDeliveries);
        } catch (Exception e) {
            log.warn("Failed to extract optimized sequence, returning original order: {}", e.getMessage());
            return new ArrayList<>(originalDeliveries);
        }
    }

    @Override
    public String getClientName() {
        return CLIENT_NAME;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String buildLocationsString(Coordinates warehouse, List<Coordinates> deliveries) {
        StringBuilder sb = new StringBuilder();
        sb.append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude());

        for (Coordinates c : deliveries) {
            sb.append(":").append(c.getLatitude()).append(",").append(c.getLongitude());
        }

        sb.append(":").append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude());
        return sb.toString();
    }

    private String buildApiUri(String locations) {
        return UriComponentsBuilder.fromUriString(TOMTOM_API_BASE_URL + "/" + locations + "/json")
                .queryParam("key", apiKey)
                .queryParam("computeBestOrder", "true")
                .queryParam("routeRepresentation", "summaryOnly")
                .queryParam("traffic", "true")
                .queryParam("routeType", "fastest")
                .toUriString();
    }

    private void logRouteMetrics(JsonNode jsonResponse) {
        if (jsonResponse.has("routes") && jsonResponse.get("routes").size() > 0) {
            JsonNode route = jsonResponse.get("routes").get(0);
            int distanceMeters = route.get("summary").get("lengthInMeters").asInt();
            int timeSeconds = route.get("summary").get("travelTimeInSeconds").asInt();

            log.info("Route calculated: {} km, {} minutes",
                    String.format("%.2f", distanceMeters / 1000.0), timeSeconds / 60);
        }
    }

    private void validateWaypointCount(JsonNode optimizedWaypoints, List<Coordinates> originalDeliveries) {
        if (optimizedWaypoints.size() != originalDeliveries.size()) {
            log.warn("Optimized waypoints count mismatch: {} vs {}",
                    optimizedWaypoints.size(), originalDeliveries.size());
        }
    }

    private List<Coordinates> reorderDeliveries(JsonNode optimizedWaypoints, List<Coordinates> originalDeliveries) {
        Coordinates[] orderedDeliveries = new Coordinates[optimizedWaypoints.size()];

        for (JsonNode waypoint : optimizedWaypoints) {
            int providedIndex = waypoint.get("providedIndex").asInt();
            int optimizedIndex = waypoint.get("optimizedIndex").asInt();

            if (isValidIndex(providedIndex, originalDeliveries.size()) && isValidIndex(optimizedIndex, orderedDeliveries.length)) {
                orderedDeliveries[optimizedIndex] = originalDeliveries.get(providedIndex);
            }
        }

        return convertToList(orderedDeliveries);
    }

    private boolean isValidIndex(int index, int size) {
        return index >= 0 && index < size;
    }

    private List<Coordinates> convertToList(Coordinates[] array) {
        List<Coordinates> result = new ArrayList<>();
        for (Coordinates coord : array) {
            if (coord != null) {
                result.add(coord);
            }
        }
        log.debug("Optimized {} deliveries", result.size());
        return result;
    }
}
