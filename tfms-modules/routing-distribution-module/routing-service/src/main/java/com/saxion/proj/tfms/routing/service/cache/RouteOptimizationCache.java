package com.saxion.proj.tfms.routing.service.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.saxion.proj.tfms.routing.model.Coordinates;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Caches route optimization results to reduce API calls.
 * Uses coordinate hash as key for efficient lookup.
 */
@Component
public class RouteOptimizationCache {

    private final ConcurrentMap<String, JsonNode> cache = new ConcurrentHashMap<>();

    /**
     * Get cached optimization result.
     */
    public JsonNode get(Coordinates warehouse, List<Coordinates> deliveries) {
        String key = generateKey(warehouse, deliveries);
        return cache.get(key);
    }

    /**
     * Cache optimization result.
     */
    public void put(Coordinates warehouse, List<Coordinates> deliveries, JsonNode result) {
        String key = generateKey(warehouse, deliveries);
        cache.put(key, result);
    }

    /**
     * Clear all cache entries.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Get cache size.
     */
    public int size() {
        return cache.size();
    }

    private String generateKey(Coordinates warehouse, List<Coordinates> deliveries) {
        StringBuilder key = new StringBuilder();
        key.append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude());
        
        for (Coordinates delivery : deliveries) {
            key.append("|").append(delivery.getLatitude()).append(",").append(delivery.getLongitude());
        }
        
        return key.toString();
    }
}
