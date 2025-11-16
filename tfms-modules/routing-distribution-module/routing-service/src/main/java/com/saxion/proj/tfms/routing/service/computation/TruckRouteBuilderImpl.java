package com.saxion.proj.tfms.routing.service.computation;

import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import com.saxion.proj.tfms.routing.model.*;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
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
        logger.infoOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTE",
                "Starting route building for warehouse ID: {} with {} truck assignments",
                warehouseId, assignmentResponse.getTruckAssignments().size());

        try {
            List<TruckRouteInfo> truckRoutes = new ArrayList<>();
            for (TruckAssignment truck : assignmentResponse.getTruckAssignments()) {
                try {
                    logger.debugOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTE",
                            "Building route for truck ID: {} in warehouse: {}",
                            truck.getTruckPlateNumber(), warehouseId);
                    TruckRouteInfo route = truckRouteFactory.createRouteForTruck(vrpRequest, truck, warehouseId);
                    route.getTotalDistance();
                    if (route != null) {
                        truckRoutes.add(route);
                    } else {
                        logger.debugOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTE",
                                "Skipped adding route for truck ID: {} - no customer deliveries",
                                truck.getTruckPlateNumber());
                    }
                } catch (Exception e) {
                    logger.errorOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTE",
                            "Failed to create route for truck ID: {} in warehouse: {} - Error: {}",
                            truck.getTruckPlateNumber(), warehouseId, e.getMessage());
                    throw new RuntimeException("Failed to create route for truck " + truck.getTruckPlateNumber(), e);
                }
            }

            logger.infoOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTE",
                    "Successfully built {} routes for warehouse ID: {}",
                    truckRoutes.size(), warehouseId);


            return WarehouseRoutingResult.builder()
                    .generatedForWarehouse(warehouseId)
                    .truckRoutes(truckRoutes)
                    .build();
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "BUILD_ROUTE",
                    "Failed to build routes for warehouse ID: {} - Error: {}",
                    warehouseId, e.getMessage());
            throw e;
        }
    }
}

