package com.saxion.proj.tfms.delivery.service;

import com.saxion.proj.tfms.truck.model.Truck;
import com.saxion.proj.tfms.delivery.dto.DeliveryOptimizationRequest;
import com.saxion.proj.tfms.delivery.dto.DeliveryOptimizationResponse;
import com.saxion.proj.tfms.delivery.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service for delivery optimization that coordinates all optimization processes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryOptimizationService {
    
    private final DataParsingService dataParsingService;
    private final RouteOptimizationService routeOptimizationService;
    
    /**
     * Optimize delivery routes from JSON data
     */
    public DeliveryOptimizationResponse optimizeFromJson(DeliveryOptimizationRequest request) {
        log.info("Starting delivery optimization from JSON data");
        
        try {
            // Parse the request data
            DataParsingService.ParsedData parsedData = dataParsingService.parseJsonData(request);
            
            // Extract all packages from warehouses
            List<DeliveryPackage> allPackages = parsedData.getWarehouses().stream()
                    .flatMap(warehouse -> warehouse.getPackages().stream())
                    .collect(Collectors.toList());
            
            // Optimize routes
            List<DeliveryRoute> optimizedRoutes = routeOptimizationService.optimizeRoutes(
                    allPackages, parsedData.getTrucks());
            
            // Build response
            return buildOptimizationResponse(optimizedRoutes, allPackages);
            
        } catch (Exception e) {
            log.error("Error during JSON optimization", e);
            throw new RuntimeException("Failed to optimize delivery routes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Optimize delivery routes from CSV data
     */
    public DeliveryOptimizationResponse optimizeFromCsv(String csvContent, List<Truck> trucks) {
        log.info("Starting delivery optimization from CSV data");
        
        try {
            // Parse CSV data
            DataParsingService.ParsedData parsedData = dataParsingService.parseCsvData(csvContent);
            
            // Use provided trucks or parsed trucks
            List<Truck> trucksToUse = trucks.isEmpty() ? parsedData.getTrucks() : trucks;
            
            // Extract all packages from warehouses
            List<DeliveryPackage> allPackages = parsedData.getWarehouses().stream()
                    .flatMap(warehouse -> warehouse.getPackages().stream())
                    .collect(Collectors.toList());
            
            // Optimize routes
            List<DeliveryRoute> optimizedRoutes = routeOptimizationService.optimizeRoutes(
                    allPackages, trucksToUse);
            
            // Build response
            return buildOptimizationResponse(optimizedRoutes, allPackages);
            
        } catch (Exception e) {
            log.error("Error during CSV optimization", e);
            throw new RuntimeException("Failed to optimize delivery routes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build optimization response from routes and packages
     */
    private DeliveryOptimizationResponse buildOptimizationResponse(
            List<DeliveryRoute> routes, List<DeliveryPackage> allPackages) {
        
        // Find unassigned packages
        Set<Long> assignedPackageIds = routes.stream()
                .flatMap(route -> route.getPackages().stream())
                .map(DeliveryPackage::getId)
                .collect(Collectors.toSet());
        
        List<DeliveryPackage> unassignedPackages = allPackages.stream()
                .filter(pkg -> !assignedPackageIds.contains(pkg.getId()))
                .collect(Collectors.toList());
        
        // Calculate total distance
        BigDecimal totalDistance = routes.stream()
                .map(DeliveryRoute::getTotalDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Build route information
        List<DeliveryOptimizationResponse.RouteInfo> routeInfos = routes.stream()
                .map(this::buildRouteInfo)
                .collect(Collectors.toList());
        
        // Build unassigned package information
        List<DeliveryOptimizationResponse.PackageInfo> unassignedPackageInfos = unassignedPackages.stream()
                .map(pkg -> buildPackageInfo(pkg))
                .collect(Collectors.toList());
        
        DeliveryOptimizationResponse response = new DeliveryOptimizationResponse();
        response.setOptimizationId(UUID.randomUUID().toString());
        response.setTotalRoutes(routes.size());
        response.setTotalPackages(allPackages.size());
        response.setTotalDistance(totalDistance);
        response.setOptimizationStatus("COMPLETED");
        response.setRoutes(routeInfos);
        response.setUnassignedPackages(unassignedPackageInfos);
        
        log.info("Optimization completed: {} routes, {} total packages, {} unassigned", 
                routes.size(), allPackages.size(), unassignedPackages.size());
        
        return response;
    }
    
    /**
     * Build route information for response
     */
    private DeliveryOptimizationResponse.RouteInfo buildRouteInfo(DeliveryRoute route) {
        DeliveryOptimizationResponse.RouteInfo routeInfo = new DeliveryOptimizationResponse.RouteInfo();
        routeInfo.setRouteId(route.getId());
        routeInfo.setTruckId(route.getTruckId());
        routeInfo.setTotalWeight(route.getTotalWeight());
        routeInfo.setPackageCount(route.getPackageCount());
        routeInfo.setEstimatedDistance(route.getTotalDistance());
        routeInfo.setEstimatedDurationMinutes(route.getEstimatedDurationMinutes());
        
        // Build package information
        List<DeliveryOptimizationResponse.PackageInfo> packageInfos = route.getPackages().stream()
                .map(pkg -> buildPackageInfo(pkg))
                .collect(Collectors.toList());
        routeInfo.setPackages(packageInfos);
        
        // Build route sequence
        List<DeliveryOptimizationResponse.Coordinate> coordinates = route.getPackages().stream()
                .map(pkg -> {
                    DeliveryOptimizationResponse.Coordinate coord = new DeliveryOptimizationResponse.Coordinate();
                    coord.setLatitude(pkg.getLatitude());
                    coord.setLongitude(pkg.getLongitude());
                    coord.setPackageName(pkg.getName());
                    return coord;
                })
                .collect(Collectors.toList());
        routeInfo.setRouteSequence(coordinates);
        
        return routeInfo;
    }
    
    /**
     * Build package information for response
     */
    private DeliveryOptimizationResponse.PackageInfo buildPackageInfo(DeliveryPackage packageItem) {
        DeliveryOptimizationResponse.PackageInfo packageInfo = new DeliveryOptimizationResponse.PackageInfo();
        packageInfo.setPackageId(packageItem.getId());
        packageInfo.setName(packageItem.getName());
        packageInfo.setWeight(packageItem.getWeight());
        packageInfo.setLatitude(packageItem.getLatitude());
        packageInfo.setLongitude(packageItem.getLongitude());
        packageInfo.setDeliveryDate(packageItem.getDeliveryDate());
        return packageInfo;
    }
    
    /**
     * Validate optimization constraints
     */
    public boolean validateConstraints(List<DeliveryRoute> routes, List<Truck> trucks) {
        for (DeliveryRoute route : routes) {
            // Find the truck for this route
            Truck truck = trucks.stream()
                    .filter(t -> t.getTruckId().equals(route.getTruckId()))
                    .findFirst()
                    .orElse(null);
            
            if (truck == null) {
                log.warn("Truck not found for route {}: {}", route.getId(), route.getTruckId());
                return false;
            }
            
            // Check weight constraints
            if (route.exceedsCapacity(truck.getWeightLimit())) {
                log.warn("Route {} exceeds truck capacity: {} > {}", 
                        route.getId(), route.getTotalWeight(), truck.getWeightLimit());
                return false;
            }
            
            // Check for duplicate packages
            Set<Long> packageIds = new HashSet<>();
            for (DeliveryPackage packageItem : route.getPackages()) {
                if (!packageIds.add(packageItem.getId())) {
                    log.warn("Duplicate package {} found in route {}", 
                            packageItem.getId(), route.getId());
                    return false;
                }
            }
        }
        
        return true;
    }
}
