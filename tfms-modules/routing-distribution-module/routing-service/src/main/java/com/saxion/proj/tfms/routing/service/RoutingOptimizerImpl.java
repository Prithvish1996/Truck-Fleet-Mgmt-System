package com.saxion.proj.tfms.routing.service;

import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;
import com.saxion.proj.tfms.routing.service.assignment.TruckAssignmentService;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.computation.TruckRouteBuilder;
import com.saxion.proj.tfms.routing.service.output.VrpResponseBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("RoutingOptimizer")
public class RoutingOptimizerImpl implements OptimizeRouting {
    private static final ServiceLogger logger = ServiceLogger.getLogger(RoutingOptimizerImpl.class);

    @Autowired
    @Qualifier("TruckAssingment")
    private TruckAssignmentService truckAssignmentService;

    @Autowired
    private TruckRouteBuilder truckRouteBuilder;

    @Autowired
    private VrpResponseBuilderService vrpResponseBuilderService;

    @Override
    public VRPResponse optimize(VRPRequest request) {
        try {
            List<WarehouseRoutingResult> warehouseRoutingResults = new ArrayList<>();
            Map<Long, AssignmentResponse> assignments =
                    truckAssignmentService.assignTrucksPerWarehouse(request);
            if (assignments == null || assignments.isEmpty()) {
                logger.warnOp(ServiceName.ROUTING_SERVICE, "Request For Optimization", "No truck assignments could be made for the given request.");
                return vrpResponseBuilderService.buildResponse(warehouseRoutingResults);
            }


            for (Map.Entry<Long, AssignmentResponse> entry : assignments.entrySet()) {
                Long warehouseId = entry.getKey();
                AssignmentResponse assignmentResponse = entry.getValue();
                if (assignmentResponse == null || !assignmentResponse.isSuccess()) {
                    logger.warnOp(ServiceName.ROUTING_SERVICE, "Request For Optimization", "No successful truck assignment for warehouse ID: {}", warehouseId);
                    continue;
                }
                try {
                    WarehouseRoutingResult warehouseRoutingResult =
                            truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse, warehouseId);
                    warehouseRoutingResults.add(warehouseRoutingResult);
                    logger.infoOp(ServiceName.ROUTING_SERVICE, "Request For Optimization", "Routes for warehouse ID: {} successfully built.", warehouseId);
                } catch (Exception e) {
                    logger.errorOp(ServiceName.ROUTING_SERVICE, "Request For Optimization", "Failed to build routes for warehouse ID: {} due to: {}", warehouseId, e.getMessage());
                    throw new RuntimeException("Failed to build routes for warehouse " + warehouseId, e);
                }
            }
            logger.infoOp(ServiceName.ROUTING_SERVICE, "Request For Optimization", "Route optimization successful for the given request.");
            return vrpResponseBuilderService.buildResponse(warehouseRoutingResults);
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "Request For Optimization", "Route optimization failed due to: {}", e.getMessage());
            throw new RuntimeException("Route optimization failed", e);
        }
    }
}
