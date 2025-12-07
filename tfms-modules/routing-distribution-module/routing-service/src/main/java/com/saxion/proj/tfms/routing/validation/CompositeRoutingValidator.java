package com.saxion.proj.tfms.routing.validation;

import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Composite validator that chains multiple validation rules.
 * Allows extensibility by adding custom validators.
 */
@Component
@org.springframework.context.annotation.Primary
public class CompositeRoutingValidator implements RoutingValidator {

    private final RoutingValidator delegate;

    @Autowired
    public CompositeRoutingValidator(RoutingValidatorImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public void validateRequest(VRPRequest request) {
        delegate.validateRequest(request);
        // Can add additional custom validators here
    }

    @Override
    public void validateAssignment(AssignmentResponse response) {
        delegate.validateAssignment(response);
        // Can add additional custom validators here
    }

    @Override
    public void validateCoordinates(RouteCoordinatesGroup coordinates) {
        delegate.validateCoordinates(coordinates);
        // Can add additional custom validators here
    }

    @Override
    public boolean hasCustomerDeliveries(List<Stop> stops) {
        return delegate.hasCustomerDeliveries(stops);
    }
}
