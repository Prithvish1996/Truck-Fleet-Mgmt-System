package com.saxion.proj.tfms.routing.service.computation.strategy;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for routing optimization strategies.
 * Manages strategy registration and selection.
 */
@Component
public class RouteOptimizationStrategyFactory {

    private final Map<String, RouteOptimizationStrategy> strategies = new ConcurrentHashMap<>();

    /**
     * Register a strategy.
     */
    public void register(String name, RouteOptimizationStrategy strategy) {
        strategies.put(name, strategy);
    }

    /**
     * Get strategy by name.
     * @throws IllegalArgumentException if strategy not found
     */
    public RouteOptimizationStrategy getStrategy(String name) {
        RouteOptimizationStrategy strategy = strategies.get(name);
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy not found: " + name);
        }
        return strategy;
    }

    /**
     * Get first available strategy.
     * Useful for fallback behavior.
     */
    public RouteOptimizationStrategy getAvailableStrategy() {
        return strategies.values().stream()
                .filter(RouteOptimizationStrategy::isAvailable)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No route optimization strategy available"));
    }

    /**
     * Get all registered strategies.
     */
    public List<String> getAvailableStrategies() {
        return strategies.keySet().stream()
                .filter(name -> strategies.get(name).isAvailable())
                .toList();
    }
}
