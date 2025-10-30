package com.saxion.proj.tfms.routing.service;

import com.saxion.proj.tfms.routing.dto.*;
import com.saxion.proj.tfms.routing.vrp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Vehicle Routing Problem solver using Google OR-Tools
 * Refactored to follow SOLID principles with proper separation of concerns
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Each class has one clear purpose
 * - Open/Closed: Extended through strategies and builders
 * - Liskov Substitution: Implements VRPProvider interface
 * - Interface Segregation: Focused interfaces for specific tasks
 * - Dependency Inversion: Depends on abstractions through @Autowired
 */
@Service
@Qualifier("OrToolsVrpService")
public class OrToolsVrpService implements VRPProvider {

    @Autowired
    private VrpRequestValidator validator;

    @Autowired
    private WarehouseGroupingStrategy warehouseStrategy;

    @Autowired
    private CapacityCalculator capacityCalculator;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private DistanceMatrixService distanceMatrixService;

    @Autowired
    private OrToolsSolver orToolsSolver;

    /**
     * Optimize truck routes for parcel delivery
     * 
     * CONSTRAINT: Each truck can only visit ONE warehouse (no mixing)
     * Multiple trucks can serve the same warehouse if needed
     */
    @Override
    public VrpResponseDto optimizeRoutes(VrpRequestDto request) throws IOException, InterruptedException {
        // Validate input using dedicated validator
        validator.validate(request);

        // Group parcels by warehouse using strategy pattern
        Map<String, WarehouseGroupingStrategy.WarehouseGroup> warehouseGroups = 
                warehouseStrategy.groupParcelsByWarehouse(request);

        // Solve VRP for each warehouse independently
        List<TruckRoute> allRoutes = new ArrayList<>();
        int totalDistance = 0;
        int totalTime = 0;
        int truckIndex = 0;

        for (WarehouseGroupingStrategy.WarehouseGroup group : warehouseGroups.values()) {

            if (!group.hasValidVolume()) {
                continue;
            }


            // Create warehouse-specific request
            VrpRequestDto warehouseRequest = warehouseStrategy.createWarehouseRequest(request, group);

            // Calculate trucks needed using capacity calculator
            int trucksNeeded = capacityCalculator.calculateTrucksNeeded(
                    group.getTotalVolume(), request.getTrucks());
            
            List<TruckInfo> availableTrucks = request.getTrucks().subList(
                    truckIndex,
                    Math.min(truckIndex + trucksNeeded, request.getTrucks().size())
            );

            if (availableTrucks.isEmpty()) {
                throw new RuntimeException("Not enough trucks available for warehouse: " + group.getWarehouseId());
            }

            warehouseRequest.setTrucks(availableTrucks);

            // Solve for this warehouse
            VrpResponseDto warehouseResponse = solveWarehouseVrp(warehouseRequest);

            // Aggregate results
            allRoutes.addAll(warehouseResponse.getTruckRoutes());
            totalDistance += warehouseResponse.getTotalDistance();
            totalTime += warehouseResponse.getTotalTime();
            truckIndex += warehouseResponse.getTotalVehiclesUsed();
        }

        // Build final response
        VrpResponseDto response = new VrpResponseDto();
        response.setTotalVehiclesUsed(allRoutes.size());
        response.setTotalDistance(totalDistance);
        response.setTotalTime(totalTime);
        response.setTruckRoutes(allRoutes);

        return response;
    }

    /**
     * Solve VRP for a single warehouse
     * Orchestrates the workflow: mapping -> distance calculation -> solving
     */
    private VrpResponseDto solveWarehouseVrp(VrpRequestDto request) throws IOException, InterruptedException {
        // Step 1: Build location mapping using LocationMapper
        LocationMapper.LocationMapping locationMapping = locationMapper.buildLocationMapping(request);

        // Step 2: Calculate distance matrix using DistanceMatrixService
        DistanceMatrixService.DistanceMatrix distanceMatrix =
                distanceMatrixService.calculateMatrix(locationMapping.getLocations());

        // Step 3: Solve using OR-Tools solver (encapsulates model building and solving)
        return orToolsSolver.solve(request, locationMapping, distanceMatrix);
    }
}
