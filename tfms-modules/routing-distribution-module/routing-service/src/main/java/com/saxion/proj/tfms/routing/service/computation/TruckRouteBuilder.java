package com.saxion.proj.tfms.routing.service.computation;

import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;

public interface TruckRouteBuilder {
    public WarehouseRoutingResult buildFullRouteForTrucks(VRPRequest vrpRequest, AssignmentResponse assignmentResponse, Long warehouseId);
}
