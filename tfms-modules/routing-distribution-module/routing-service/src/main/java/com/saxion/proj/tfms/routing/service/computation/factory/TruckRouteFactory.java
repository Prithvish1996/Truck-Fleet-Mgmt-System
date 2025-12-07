package com.saxion.proj.tfms.routing.service.computation.factory;

import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.model.TruckRouteInfo;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;
import com.saxion.proj.tfms.routing.service.computation.helper.RoutingProvider;
import com.saxion.proj.tfms.routing.validation.RoutingValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TruckRouteFactory {

    private static final ServiceLogger logger = ServiceLogger.getLogger(TruckRouteFactory.class);

    @Autowired
    private RouteDataPreparer dataPreparer;

    @Autowired
    @Qualifier("RoutingProblemSolver")
    private RoutingProvider routingProvider;
    
    @Autowired
    private RoutingValidator validator;

    public TruckRouteInfo createRouteForTruck(VRPRequest vrpRequest, TruckAssignment assignment, Long warehouseId) {
        logger.infoOp(ServiceName.ROUTING_SERVICE, "CREATE_TRUCK_ROUTE",
                "Creating route for truck: {} in warehouse: {} with {} parcels",
                assignment.getTruckPlateNumber(), warehouseId, assignment.getParcels().size());

        try {
            RouteCoordinatesGroup coords = dataPreparer.prepareCoordinates(vrpRequest, assignment);
            validator.validateCoordinates(coords);
            
            logger.debugOp(ServiceName.ROUTING_SERVICE, "CREATE_TRUCK_ROUTE",
                    "Coordinates prepared for truck: {}", assignment.getTruckPlateNumber());

            List<Stop> stops = routingProvider.calculateRoute(coords);

            if (!validator.hasCustomerDeliveries(stops)) {
                logger.warnOp(ServiceName.ROUTING_SERVICE, "CREATE_TRUCK_ROUTE",
                        "Skipping route creation for truck: {} - no customer deliveries",
                        assignment.getTruckPlateNumber());
                return null;
            }

            TruckRouteInfo routeInfo = buildTruckRoute(vrpRequest, assignment, stops);

            logger.infoOp(ServiceName.ROUTING_SERVICE, "CREATE_TRUCK_ROUTE",
                    "Successfully created route for truck: {} with {} stops",
                    assignment.getTruckPlateNumber(), stops.size());

            return routeInfo;
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "CREATE_TRUCK_ROUTE",
                    "Failed to create route for truck: {} - Error: {}",
                    assignment.getTruckPlateNumber(), e.getMessage());
            throw new RuntimeException("Failed to create route for truck " + assignment.getTruckPlateNumber(), e);
        }
    }

    private TruckRouteInfo buildTruckRoute(VRPRequest vrpRequest, TruckAssignment assignment, List<Stop> stops) {
        return TruckRouteInfo.builder()
                .truckPlateNumber(assignment.getTruckPlateNumber())
                .depotId(vrpRequest.getDepot().getDepotId())
                .depotName(vrpRequest.getDepot().getDepotName())
                .routeStops(stops)
                .totalDistance(0)
                .totalTransportTime(0L)
                .build();
    }
}

