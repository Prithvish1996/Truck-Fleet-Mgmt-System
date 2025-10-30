package com.saxion.proj.tfms.routing.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@Service
public class DistanceMatrixService {

    private static final String OSRM_TABLE_URL = "http://router.project-osrm.org/table/v1/driving/";
    private final HttpClient httpClient;

    public DistanceMatrixService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Location coordinate
     */
    public static class Location {
        public final double latitude;
        public final double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Distance matrix result
     */
    public static class DistanceMatrix {
        public final long[][] distanceMatrix;  // in meters
        public final long[][] durationMatrix;  // in seconds

        public DistanceMatrix(long[][] distanceMatrix, long[][] durationMatrix) {
            this.distanceMatrix = distanceMatrix;
            this.durationMatrix = durationMatrix;
        }
    }

    /**
     * Calculate distance and time matrix for given locations using OSRM
     */
    public DistanceMatrix calculateMatrix(List<Location> locations) throws IOException, InterruptedException {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Locations list cannot be empty");
        }

        StringBuilder coordinates = new StringBuilder();
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            if (i > 0) {
                coordinates.append(";");
            }
            coordinates.append(loc.longitude).append(",").append(loc.latitude);
        }

        String url = OSRM_TABLE_URL + coordinates.toString() + "?annotations=distance,duration";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OSRM API error: " + response.body());
        }

        return parseOsrmResponse(response.body(), locations.size());
    }

    private DistanceMatrix parseOsrmResponse(String responseBody, int numLocations) {
        JSONObject json = new JSONObject(responseBody);

        if (!json.getString("code").equals("Ok")) {
            throw new RuntimeException("OSRM API returned error: " + json.optString("message", "Unknown error"));
        }

        JSONArray distancesArray = json.getJSONArray("distances");
        long[][] distanceMatrix = new long[numLocations][numLocations];
        for (int i = 0; i < numLocations; i++) {
            JSONArray row = distancesArray.getJSONArray(i);
            for (int j = 0; j < numLocations; j++) {
                distanceMatrix[i][j] = row.getLong(j);
            }
        }

        JSONArray durationsArray = json.getJSONArray("durations");
        long[][] durationMatrix = new long[numLocations][numLocations];
        for (int i = 0; i < numLocations; i++) {
            JSONArray row = durationsArray.getJSONArray(i);
            for (int j = 0; j < numLocations; j++) {
                durationMatrix[i][j] = row.getLong(j);
            }
        }

        return new DistanceMatrix(distanceMatrix, durationMatrix);
    }

    public long calculateHaversineDistance(Location loc1, Location loc2) {
        final int EARTH_RADIUS = 6371000;

        double lat1Rad = Math.toRadians(loc1.latitude);
        double lat2Rad = Math.toRadians(loc2.latitude);
        double deltaLat = Math.toRadians(loc2.latitude - loc1.latitude);
        double deltaLon = Math.toRadians(loc2.longitude - loc1.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(EARTH_RADIUS * c);
    }

    public DistanceMatrix calculateHaversineMatrix(List<Location> locations) {
        int n = locations.size();
        long[][] distanceMatrix = new long[n][n];
        long[][] durationMatrix = new long[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                    durationMatrix[i][j] = 0;
                } else {
                    long distance = calculateHaversineDistance(locations.get(i), locations.get(j));
                    distanceMatrix[i][j] = distance;
                    durationMatrix[i][j] = (long) (distance / 13.89);
                }
            }
        }

        return new DistanceMatrix(distanceMatrix, durationMatrix);
    }
}
