package com.saxion.proj.tfms.routing.service.assignment;

import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;

import java.util.Map;

public interface TruckAssignmentService {
   public Map<Long, AssignmentResponse> assignTrucksPerWarehouse(VRPRequest vrpRequest);
}
