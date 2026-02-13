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
import com.saxion.proj.tfms.routing.validation.RoutingValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("RoutingOptimizer")
public class RoutingOptimizerImpl implements OptimizeRouting {
    private static final ServiceLogger logger = ServiceLogger.getLogger(RoutingOptimizerImpl.class);

    private final TruckAssignmentService truckAssignmentService;
    private final TruckRouteBuilder truckRouteBuilder;
    private final VrpResponseBuilderService vrpResponseBuilderService;
    private final RoutingValidator validator;

    @Autowired
    public RoutingOptimizerImpl(
            @Qualifier("TruckAssignment") TruckAssignmentService truckAssignmentService,
            TruckRouteBuilder truckRouteBuilder,
            VrpResponseBuilderService vrpResponseBuilderService,
            RoutingValidator validator) {
        this.truckAssignmentService = truckAssignmentService;
        this.truckRouteBuilder = truckRouteBuilder;
        this.vrpResponseBuilderService = vrpResponseBuilderService;
        this.validator = validator;
    }

    @Override
    public VRPResponse optimize(VRPRequest request) {
        try {
            validator.validateRequest(request);
            
            List<WarehouseRoutingResult> warehouseRoutingResults = new ArrayList<>();
            Map<Long, AssignmentResponse> assignments =
                    truckAssignmentService.assignTrucksPerWarehouse(request);
            
            if (assignments == null || assignments.isEmpty()) {
                logger.warnOp(ServiceName.ROUTING_SERVICE, "OPTIMIZE",
                        "No truck assignments could be made for the given request.");
                return vrpResponseBuilderService.buildResponse(warehouseRoutingResults);
            }

            for (Map.Entry<Long, AssignmentResponse> entry : assignments.entrySet()) {
                Long warehouseId = entry.getKey();
                AssignmentResponse assignmentResponse = entry.getValue();
                
                if (assignmentResponse == null || !assignmentResponse.isSuccess()) {
                    logger.warnOp(ServiceName.ROUTING_SERVICE, "OPTIMIZE",
                            "No successful truck assignment for warehouse ID: {}", warehouseId);
                    continue;
                }
                
                try {
                    validator.validateAssignment(assignmentResponse);
                    
                    WarehouseRoutingResult warehouseRoutingResult =
                            truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse, warehouseId);
                    warehouseRoutingResults.add(warehouseRoutingResult);
                    
                    logger.infoOp(ServiceName.ROUTING_SERVICE, "OPTIMIZE",
                            "Routes for warehouse ID: {} successfully built.", warehouseId);
                } catch (Exception e) {
                    logger.errorOp(ServiceName.ROUTING_SERVICE, "OPTIMIZE",
                            "Failed to build routes for warehouse ID: {}: {}", warehouseId, e.getMessage());
                    throw new RuntimeException("Failed to build routes for warehouse " + warehouseId, e);
                }
            }
            
            logger.infoOp(ServiceName.ROUTING_SERVICE, "OPTIMIZE",
                    "Route optimization successful for the given request.");
            return vrpResponseBuilderService.buildResponse(warehouseRoutingResults);
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "OPTIMIZE",
                    "Route optimization failed: {}", e.getMessage());
            throw new RuntimeException("Route optimization failed", e);
        }
    }
}
