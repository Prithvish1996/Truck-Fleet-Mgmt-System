package com.saxion.proj.tfms.routing.service.assignment.impl;



import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.TruckAssignmentService;
import com.saxion.proj.tfms.routing.service.assignment.helper.CapacityAssignmentManager;
import com.saxion.proj.tfms.routing.service.assignment.helper.IGetAllTrucksAvailable;
import com.saxion.proj.tfms.routing.service.assignment.helper.IMarkTruckUnavailable;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.service.TruckAssingmentAlgoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service("TruckAssignment")
public class TruckAssignmentServiceImpl implements TruckAssignmentService {

    @Autowired
    private CapacityAssignmentManager manager;



    @Override
    public Map<Long, AssignmentResponse> assignTrucksPerWarehouse(VRPRequest vrpRequest) {

        return manager.manageAssignmentPerWarehouse(vrpRequest);
    }
}
