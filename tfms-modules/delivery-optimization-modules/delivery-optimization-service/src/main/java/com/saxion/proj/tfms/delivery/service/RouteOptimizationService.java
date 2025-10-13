package com.saxion.proj.tfms.delivery.service;

import com.saxion.proj.tfms.truck.model.Truck;
import com.saxion.proj.tfms.delivery.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for optimizing delivery routes using TSP-based algorithms with capacity constraints
 */
@Service
@Slf4j
public class RouteOptimizationService {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Optimize delivery routes for given packages and trucks
     */
    public List<DeliveryRoute> optimizeRoutes(List<DeliveryPackage> packages, List<Truck> trucks) {
        log.info("Starting route optimization for {} packages and {} trucks", 
                packages.size(), trucks.size());
        
        if (packages.isEmpty() || trucks.isEmpty()) {
            log.warn("No packages or trucks provided for optimization");
            return new ArrayList<>();
        }
        
        // Sort trucks by capacity (largest first) for better utilization
        List<Truck> sortedTrucks = trucks.stream()
                .sorted((t1, t2) -> t2.getWeightLimit().compareTo(t1.getWeightLimit()))
                .collect(Collectors.toList());
        
        List<DeliveryRoute> optimizedRoutes = new ArrayList<>();
        List<DeliveryPackage> remainingPackages = new ArrayList<>(packages);
        
        // Create routes for each truck
        for (Truck truck : sortedTrucks) {
            if (remainingPackages.isEmpty()) {
                break;
            }
            
            DeliveryRoute route = createOptimizedRoute(truck.getTruckId(), truck, remainingPackages);
            if (!route.getPackages().isEmpty()) {
                optimizedRoutes.add(route);
                // Remove assigned packages from remaining list
                remainingPackages.removeAll(route.getPackages());
                log.info("Created route for truck {} with {} packages", 
                        truck.getTruckId(), route.getPackageCount());
            }
        }
        
        log.info("Route optimization completed. Created {} routes, {} packages unassigned", 
                optimizedRoutes.size(), remainingPackages.size());
        
        return optimizedRoutes;
    }
    
    /**
     * Create an optimized route for a single truck using nearest neighbor with capacity constraints
     */
    private DeliveryRoute createOptimizedRoute(String truckId, Truck truck, List<DeliveryPackage> availablePackages) {
        DeliveryRoute route = new DeliveryRoute(truckId);
        List<DeliveryPackage> routePackages = new ArrayList<>();
        BigDecimal currentWeight = BigDecimal.ZERO;
        
        // Start from warehouse (assuming first package's warehouse as starting point)
        DeliveryPackage currentLocation = availablePackages.get(0);
        List<DeliveryPackage> unvisited = new ArrayList<>(availablePackages);
        
        while (!unvisited.isEmpty()) {
            DeliveryPackage nearestPackage = findNearestPackage(currentLocation, unvisited, 
                    truck.getWeightLimit().subtract(currentWeight));
            
            if (nearestPackage == null) {
                break; // No more packages can fit
            }
            
            // Add package to route
            routePackages.add(nearestPackage);
            currentWeight = currentWeight.add(nearestPackage.getWeight());
            unvisited.remove(nearestPackage);
            currentLocation = nearestPackage;
        }
        
        // Optimize the route using 2-opt improvement
        routePackages = optimizeRouteSequence(routePackages);
        
        // Add packages to route
        for (DeliveryPackage packageItem : routePackages) {
            route.addPackage(packageItem);
        }
        
        // Calculate route metrics
        calculateRouteMetrics(route);
        
        return route;
    }
    
    /**
     * Find the nearest package that fits within weight constraints
     */
    private DeliveryPackage findNearestPackage(DeliveryPackage currentLocation, List<DeliveryPackage> candidates, 
            BigDecimal remainingCapacity) {
        DeliveryPackage nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (DeliveryPackage candidate : candidates) {
            if (candidate.getWeight().compareTo(remainingCapacity) <= 0) {
                double distance = calculateDistance(currentLocation, candidate);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = candidate;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Optimize route sequence using 2-opt algorithm
     */
    private List<DeliveryPackage> optimizeRouteSequence(List<DeliveryPackage> packages) {
        if (packages.size() <= 2) {
            return packages;
        }
        
        List<DeliveryPackage> bestRoute = new ArrayList<>(packages);
        boolean improved = true;
        int maxIterations = 100;
        int iterations = 0;
        
        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;
            
            for (int i = 1; i < bestRoute.size() - 1; i++) {
                for (int j = i + 1; j < bestRoute.size(); j++) {
                    List<DeliveryPackage> newRoute = twoOptSwap(bestRoute, i, j);
                    if (calculateTotalDistance(newRoute) < calculateTotalDistance(bestRoute)) {
                        bestRoute = newRoute;
                        improved = true;
                    }
                }
            }
        }
        
        return bestRoute;
    }
    
    /**
     * Perform 2-opt swap operation
     */
    private List<DeliveryPackage> twoOptSwap(List<DeliveryPackage> route, int i, int j) {
        List<DeliveryPackage> newRoute = new ArrayList<>();
        
        // Take route[0] to route[i-1] and add them in order
        for (int k = 0; k < i; k++) {
            newRoute.add(route.get(k));
        }
        
        // Take route[i] to route[j] and add them in reverse order
        for (int k = j; k >= i; k--) {
            newRoute.add(route.get(k));
        }
        
        // Take route[j+1] to end and add them in order
        for (int k = j + 1; k < route.size(); k++) {
            newRoute.add(route.get(k));
        }
        
        return newRoute;
    }
    
    /**
     * Calculate total distance for a route
     */
    private double calculateTotalDistance(List<DeliveryPackage> packages) {
        if (packages.size() <= 1) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 0; i < packages.size() - 1; i++) {
            totalDistance += calculateDistance(packages.get(i), packages.get(i + 1));
        }
        
        return totalDistance;
    }
    
    /**
     * Calculate distance between two packages using Haversine formula
     */
    private double calculateDistance(DeliveryPackage p1, DeliveryPackage p2) {
        double lat1 = p1.getLatitude().doubleValue();
        double lon1 = p1.getLongitude().doubleValue();
        double lat2 = p2.getLatitude().doubleValue();
        double lon2 = p2.getLongitude().doubleValue();
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Calculate route metrics (distance, duration)
     */
    private void calculateRouteMetrics(DeliveryRoute route) {
        if (route.getPackages().isEmpty()) {
            route.setTotalDistance(BigDecimal.ZERO);
            route.setEstimatedDurationMinutes(0);
            return;
        }
        
        double totalDistanceKm = calculateTotalDistance(route.getPackages());
        route.setTotalDistance(BigDecimal.valueOf(totalDistanceKm).setScale(2, RoundingMode.HALF_UP));
        
        // Estimate duration: assume average speed of 50 km/h + 10 minutes per delivery
        int deliveryTime = route.getPackageCount() * 10; // 10 minutes per delivery
        int travelTime = (int) Math.ceil(totalDistanceKm / 50.0 * 60); // Convert to minutes
        route.setEstimatedDurationMinutes(deliveryTime + travelTime);
        
        // Create route sequence for navigation
        List<String> sequence = route.getPackages().stream()
                .map(p -> String.format("%.6f,%.6f", 
                        p.getLatitude().doubleValue(), 
                        p.getLongitude().doubleValue()))
                .collect(Collectors.toList());
        route.setRouteSequence(String.join(";", sequence));
    }
}
