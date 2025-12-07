package com.saxion.proj.tfms.routing.service.computation.preparation;

import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;

/**
 * Data preparation contract.
 * Isolates data transformation logic from business orchestration.
 */
public interface RouteDataPreparationService {

    /**
     * Transform truck assignment and VRP request into coordinate groups.
     */
    RouteCoordinatesGroup prepareCoordinates(VRPRequest vrpRequest, TruckAssignment assignment);

    /**
     * Validate that preparation is feasible.
     */
    boolean canPrepare(VRPRequest vrpRequest, TruckAssignment assignment);
}
