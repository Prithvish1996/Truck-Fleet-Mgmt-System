package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import com.saxion.proj.tfms.routing.model.ClusterResult;
import com.saxion.proj.tfms.routing.model.Coordinates;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class TomTomRouteCalculator {

    private static final String TOMTOM_ROUTE_URL = "https://api.tomtom.com/routing/1/calculateRoute/{locations}/json";
    private static final String API_KEY = "YOUR_TOMTOM_API_KEY";

    private final RestTemplate restTemplate;

    public TomTomRouteCalculator() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Compute best route for each cluster and return total distance & time.
     */
    public void computeBestRoutePerCluster(Coordinates warehouse, ClusterResult clusterResult) {

        Map<Integer, List<Coordinates>> clusters = clusterResult.getShiftClusters();

        for (Map.Entry<Integer, List<Coordinates>> entry : clusters.entrySet()) {
            int shiftIndex = entry.getKey();
            List<Coordinates> deliveries = entry.getValue();

            // Skip empty clusters
            if (deliveries.isEmpty()) continue;

            // Build locations string for TomTom API: start;via;via;...;end
            StringBuilder locationsBuilder = new StringBuilder();
            locationsBuilder.append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude()); // start
            for (Coordinates c : deliveries) {
                locationsBuilder.append(":").append(c.getLatitude()).append(",").append(c.getLongitude());
            }
            locationsBuilder.append(":").append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude()); // return to warehouse if needed

            // Build URI with API key
            String uri = UriComponentsBuilder.fromUriString(TOMTOM_ROUTE_URL)
                    .buildAndExpand(locationsBuilder.toString())
                    .toUriString() + "?key=" + API_KEY;

            // Call TomTom API
            TomTomRouteResponse response = restTemplate.getForObject(uri, TomTomRouteResponse.class);

            if (response != null && !response.getRoutes().isEmpty()) {
                int totalDistanceMeters = response.getRoutes().get(0).getSummary().getLengthInMeters();
                int totalTimeSeconds = response.getRoutes().get(0).getSummary().getTravelTimeInSeconds();

                System.out.println("Shift " + shiftIndex + ":");
                System.out.println("  Deliveries: " + deliveries.size());
                System.out.println("  Total Distance (m): " + totalDistanceMeters);
                System.out.println("  Total Time (sec): " + totalTimeSeconds);
            }
        }
    }

    // DTO for TomTom response (simplified)
    public static class TomTomRouteResponse {
        private List<Route> routes;

        public List<Route> getRoutes() { return routes; }
        public void setRoutes(List<Route> routes) { this.routes = routes; }

        public static class Route {
            private Summary summary;
            public Summary getSummary() { return summary; }
            public void setSummary(Summary summary) { this.summary = summary; }

            public static class Summary {
                private int lengthInMeters;
                private int travelTimeInSeconds;

                public int getLengthInMeters() { return lengthInMeters; }
                public void setLengthInMeters(int lengthInMeters) { this.lengthInMeters = lengthInMeters; }

                public int getTravelTimeInSeconds() { return travelTimeInSeconds; }
                public void setTravelTimeInSeconds(int travelTimeInSeconds) { this.travelTimeInSeconds = travelTimeInSeconds; }
            }
        }
    }
}
