package com.saxion.proj.tfms.commons.utility.depotwarehouse.helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * GraphHopper API utility for routing
 * Uses GraphHopper Directions API (cloud-based, no map files needed)
 * 
 * Traffic Support:
 * - Basic routing: Considers road types, speed limits, turn restrictions
 * - Traffic signals: Built into road network (average delay included)
 * - Real-time traffic: Requires premium API (ch.disable=true parameter)
 */
public class GraphHopperUtil {

    private final String apiKey;
    private final String profile;
    private final boolean useTraffic;
    private final HttpClient httpClient;
    private static final String API_BASE_URL = "https://graphhopper.com/api/1/route";

    private GraphHopperUtil(String apiKey, String profile, boolean useTraffic) {
        this.apiKey = apiKey;
        this.profile = profile;
        this.useTraffic = useTraffic;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Create GraphHopperUtil with hosted API
     * @param apiKey Your GraphHopper API key
     * @param profile Routing profile: "car", "truck", "bike", "foot", "motorcycle"
     */
    public static GraphHopperUtil withHostedAPI(String apiKey, String profile) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("GraphHopper API key is required");
        }
        return new GraphHopperUtil(apiKey, profile, false);
    }

    /**
     * Create GraphHopperUtil with traffic-aware routing (Premium feature)
     * @param apiKey Your GraphHopper API key (requires premium plan)
     * @param profile Routing profile
     * @param useTraffic Enable real-time traffic data
     */
    public static GraphHopperUtil withTraffic(String apiKey, String profile, boolean useTraffic) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("GraphHopper API key is required");
        }
        return new GraphHopperUtil(apiKey, profile, useTraffic);
    }

    /**
     * Compute distance in km using GraphHopper API
     */
    public double computeDistance(double fromLat, double fromLon, double toLat, double toLon) {
        try {
            String url = String.format("%s?point=%f,%f&point=%f,%f&profile=%s&key=%s",
                    API_BASE_URL, fromLat, fromLon, toLat, toLon, profile, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONArray paths = json.getJSONArray("paths");
                
                if (!paths.isEmpty()) {
                    double distanceMeters = paths.getJSONObject(0).getDouble("distance");
                    return distanceMeters / 1000.0; // Convert to km
                }
            } else {
                System.err.println("GraphHopper API error: " + response.statusCode() + " - " + response.body());
            }

            // Fallback to straight-line distance
            return haversineDistance(fromLat, fromLon, toLat, toLon);

        } catch (Exception e) {
            System.err.println("GraphHopper routing error: " + e.getMessage());
            return haversineDistance(fromLat, fromLon, toLat, toLon);
        }
    }

    /**
     * Compute travel time in minutes using GraphHopper API
     */
    public double computeTime(double fromLat, double fromLon, double toLat, double toLon) {
        try {
            String url = String.format("%s?point=%f,%f&point=%f,%f&profile=%s&key=%s",
                    API_BASE_URL, fromLat, fromLon, toLat, toLon, profile, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONArray paths = json.getJSONArray("paths");
                
                if (paths.length() > 0) {
                    long timeMillis = paths.getJSONObject(0).getLong("time");
                    return timeMillis / 60000.0; // Convert to minutes
                }
            } else {
                System.err.println("GraphHopper API error: " + response.statusCode() + " - " + response.body());
            }

            // Fallback estimate: 50 km/h average speed
            double distance = haversineDistance(fromLat, fromLon, toLat, toLon);
            return (distance / 50.0) * 60.0;

        } catch (Exception e) {
            System.err.println("GraphHopper routing error: " + e.getMessage());
            double distance = haversineDistance(fromLat, fromLon, toLat, toLon);
            return (distance / 50.0) * 60.0;
        }
    }

    /**
     * Calculate straight-line distance using Haversine formula (fallback)
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
