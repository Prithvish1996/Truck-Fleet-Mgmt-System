package com.saxion.proj.tfms.routing.validation;

import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of routing validation.
 * Extracted for testability and separation of concerns.
 */
@Component
public class RoutingValidatorImpl implements RoutingValidator {
    // Non-primary validator implementation

    private static final String NULL_REQUEST = "VRP request cannot be null";
    private static final String NULL_DEPOT = "Depot information is required";
    private static final String EMPTY_PARCELS = "At least one parcel must be provided";
    private static final String NULL_ASSIGNMENT = "Assignment response cannot be null";
    private static final String NULL_COORDINATES = "Route coordinates cannot be null";
    private static final String INVALID_DEPOT = "Depot coordinates are invalid";
    private static final String INVALID_WAREHOUSE = "Warehouse coordinates are invalid";

    @Override
    public void validateRequest(VRPRequest request) {
        Objects.requireNonNull(request, NULL_REQUEST);
        Objects.requireNonNull(request.getDepot(), NULL_DEPOT);
        if (request.getParcels() == null || request.getParcels().isEmpty()) {
            throw new IllegalArgumentException(EMPTY_PARCELS);
        }
    }

    @Override
    public void validateAssignment(AssignmentResponse response) {
        Objects.requireNonNull(response, NULL_ASSIGNMENT);
    }

    @Override
    public void validateCoordinates(RouteCoordinatesGroup coordinates) {
        Objects.requireNonNull(coordinates, NULL_COORDINATES);
        Objects.requireNonNull(coordinates.getDepot(), INVALID_DEPOT);
        Objects.requireNonNull(coordinates.getWarehouse(), INVALID_WAREHOUSE);
    }

    @Override
    public boolean hasCustomerDeliveries(List<Stop> stops) {
        return stops != null && stops.stream()
                .anyMatch(stop -> stop.getStopType() == StopType.CUSTOMER);
    }
}
