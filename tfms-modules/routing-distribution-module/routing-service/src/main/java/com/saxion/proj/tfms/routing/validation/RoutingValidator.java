package com.saxion.proj.tfms.routing.validation;

import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import java.util.List;

/**
 * Contract for validation logic in the routing domain.
 * Separates validation concerns from business logic.
 */
public interface RoutingValidator {

    /**
     * Validate incoming optimization request.
     * @throws IllegalArgumentException if validation fails
     */
    void validateRequest(VRPRequest request);

    /**
     * Validate truck assignment response.
     * @throws IllegalArgumentException if validation fails
     */
    void validateAssignment(AssignmentResponse response);

    /**
     * Validate route coordinates group.
     * @throws IllegalArgumentException if validation fails
     */
    void validateCoordinates(RouteCoordinatesGroup coordinates);

    /**
     * Validate calculated route has meaningful deliveries.
     * @return true if route has customer deliveries, false if only depot/warehouse stops
     */
    boolean hasCustomerDeliveries(List<Stop> stops);
}
