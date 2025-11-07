package com.saxion.proj.tfms.routing.service;


import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;
import com.saxion.proj.tfms.routing.service.assignment.TruckAssignmentService;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.computation.TruckRouteBuilder;
import com.saxion.proj.tfms.routing.service.output.VrpResponseBuilderService;
import com.saxion.proj.tfms.routing.service.output.VrpResponseBuilderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("RoutingOptimizer")
public class RoutingOptimizerImpl implements OptimizeRoting {

    @Autowired
    @Qualifier("TruckAssingment")
    private TruckAssignmentService truckAssignmentService;

    @Autowired
    private TruckRouteBuilder truckRouteBuilder;

    @Autowired
    private VrpResponseBuilderService vrpResponseBuilderService;

    @Override
    public VRPResponse optimize(VRPRequest request) {
        List<WarehouseRoutingResult> warehouseRoutingResults = new ArrayList<>();
        Map<Long, AssignmentResponse> assignments =
                truckAssignmentService.assignTrucksPerWarehouse(request);
        for (Map.Entry<Long, AssignmentResponse> entry : assignments.entrySet()) {
            Long warehouseId = entry.getKey();
            AssignmentResponse assignmentResponse = entry.getValue();
            WarehouseRoutingResult warehouseRoutingResult =
                    truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse, warehouseId);
            warehouseRoutingResults.add(warehouseRoutingResult);
        }
        return vrpResponseBuilderService.buildResponse(warehouseRoutingResults);
    }
}


