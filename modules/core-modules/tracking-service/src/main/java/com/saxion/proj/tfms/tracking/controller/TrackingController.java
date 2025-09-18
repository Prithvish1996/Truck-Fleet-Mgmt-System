package com.saxion.proj.tfms.tracking.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private Map<Long, Map<String, Object>> trackingData = new HashMap<>();

    @GetMapping("/{truckId}")
    public Map<String, Object> getTrackingData(@PathVariable Long truckId) {
        return trackingData.getOrDefault(truckId, createDefaultTrackingData(truckId));
    }

    @PostMapping("/{truckId}/location")
    public Map<String, Object> updateLocation(@PathVariable Long truckId, @RequestBody Map<String, Object> locationData) {
        Map<String, Object> tracking = trackingData.computeIfAbsent(truckId, k -> createDefaultTrackingData(truckId));
        
        tracking.put("latitude", locationData.get("latitude"));
        tracking.put("longitude", locationData.get("longitude"));
        tracking.put("lastUpdated", LocalDateTime.now());
        tracking.put("status", "MOVING");

        // Add to location history
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = (List<Map<String, Object>>) tracking.get("locationHistory");
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("latitude", locationData.get("latitude"));
        historyEntry.put("longitude", locationData.get("longitude"));
        historyEntry.put("timestamp", LocalDateTime.now());
        history.add(historyEntry);

        // Keep only last 50 entries
        if (history.size() > 50) {
            history.remove(0);
        }

        return tracking;
    }

    @GetMapping("/{truckId}/route")
    public Map<String, Object> getRoute(@PathVariable Long truckId) {
        Map<String, Object> tracking = trackingData.get(truckId);
        if (tracking != null) {
            return Map.of(
                "truckId", truckId,
                "currentLocation", Map.of(
                    "latitude", tracking.get("latitude"),
                    "longitude", tracking.get("longitude")
                ),
                "locationHistory", tracking.get("locationHistory")
            );
        }
        return Map.of("error", "No tracking data found for truck " + truckId);
    }

    @GetMapping("/all")
    public Map<String, Object> getAllTrackingData() {
        return Map.of("trucks", trackingData);
    }

    @PostMapping("/{truckId}/status")
    public Map<String, Object> updateStatus(@PathVariable Long truckId, @RequestBody Map<String, String> statusData) {
        Map<String, Object> tracking = trackingData.computeIfAbsent(truckId, k -> createDefaultTrackingData(truckId));
        tracking.put("status", statusData.get("status"));
        tracking.put("lastUpdated", LocalDateTime.now());
        return tracking;
    }

    private Map<String, Object> createDefaultTrackingData(Long truckId) {
        Map<String, Object> tracking = new HashMap<>();
        tracking.put("truckId", truckId);
        tracking.put("latitude", 52.2215);  // Default to Enschede coordinates
        tracking.put("longitude", 6.8937);
        tracking.put("status", "IDLE");
        tracking.put("lastUpdated", LocalDateTime.now());
        tracking.put("locationHistory", new ArrayList<Map<String, Object>>());
        return tracking;
    }
}
