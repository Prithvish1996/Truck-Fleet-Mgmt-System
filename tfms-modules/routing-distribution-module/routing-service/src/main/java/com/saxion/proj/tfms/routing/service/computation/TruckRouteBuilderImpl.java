package com.saxion.proj.tfms.routing.service.computation;

import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import com.saxion.proj.tfms.routing.model.*;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.computation.factory.TruckRouteFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TruckRouteBuilderImpl implements TruckRouteBuilder {

    private static final ServiceLogger logger = ServiceLogger.getLogger(TruckRouteBuilderImpl.class);

    @Autowired
    private TruckRouteFactory truckRouteFactory;

    @Override
    public WarehouseRoutingResult buildFullRouteForTrucks(VRPRequest vrpRequest, AssignmentResponse assignmentResponse, Long warehouseId) {
        logger.infoOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTES",
                "Building routes for warehouse: {} with {} assignments",
                warehouseId, assignmentResponse.getTruckAssignments().size());

        try {
            List<TruckRouteInfo> truckRoutes = buildRoutesForAllTrucks(vrpRequest, assignmentResponse, warehouseId);

            logger.infoOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTES",
                    "Successfully built {} routes for warehouse: {}",
                    truckRoutes.size(), warehouseId);

            return WarehouseRoutingResult.builder()
                    .generatedForWarehouse(warehouseId)
                    .truckRoutes(truckRoutes)
                    .build();
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTES",
                    "Failed to build routes for warehouse: {}: {}",
                    warehouseId, e.getMessage());
            throw e;
        }
    }

    private List<TruckRouteInfo> buildRoutesForAllTrucks(VRPRequest vrpRequest, AssignmentResponse assignmentResponse, Long warehouseId) {
        List<TruckRouteInfo> truckRoutes = new ArrayList<>();

        for (TruckAssignment truck : assignmentResponse.getTruckAssignments()) {
            try {
                TruckRouteInfo route = buildRouteForSingleTruck(vrpRequest, truck, warehouseId);
                
                if (route != null) {
                    truckRoutes.add(route);
                } else {
                    logger.debugOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTES",
                            "Skipped route for truck: {} - no customer deliveries",
                            truck.getTruckPlateNumber());
                }
            } catch (Exception e) {
                logger.errorOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTES",
                        "Failed for truck: {}: {}",
                        truck.getTruckPlateNumber(), e.getMessage());
                throw new RuntimeException("Failed to create route for truck " + truck.getTruckPlateNumber(), e);
            }
        }

        return truckRoutes;
    }

    private TruckRouteInfo buildRouteForSingleTruck(VRPRequest vrpRequest, TruckAssignment truck, Long warehouseId) {
        logger.debugOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTES",
                "Processing truck: {} in warehouse: {}",
                truck.getTruckPlateNumber(), warehouseId);
        
        return truckRouteFactory.createRouteForTruck(vrpRequest, truck, warehouseId);
    }
}

