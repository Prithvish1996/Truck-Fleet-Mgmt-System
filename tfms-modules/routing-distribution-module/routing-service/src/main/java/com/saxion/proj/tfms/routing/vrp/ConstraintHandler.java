package com.saxion.proj.tfms.routing.vrp;

import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.saxion.proj.tfms.routing.dto.ParcelInfo;
import com.saxion.proj.tfms.routing.dto.VrpRequestDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles OR-Tools constraint definitions for VRP optimization.
 * Single Responsibility Principle: Manages only constraint logic
 */
@Component
public class ConstraintHandler {

    public void addCapacityConstraints(RoutingModel routing, RoutingIndexManager manager,
                                       VrpRequestDto request, LocationMapper.LocationMapping locationMapping) {
        
        final long[] demands = buildDemandsArray(request, locationMapping);

        final long[] vehicleCapacities = request.getTrucks().stream()
                .mapToLong(truck -> Math.round(truck.getVolume()))
                .toArray();

        final int demandCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return demands[fromNode];
        });

        routing.addDimensionWithVehicleCapacity(
                demandCallbackIndex,
                0L,
                vehicleCapacities,
                true,
                "Capacity"
        );

    }

    public void addPickupDeliveryConstraints(RoutingModel routing, RoutingIndexManager manager,
                                             VrpRequestDto request, LocationMapper.LocationMapping locationMapping) {
        
        RoutingDimension timeDimension = routing.getMutableDimension("Capacity");
        
        for (ParcelInfo parcel : request.getParcels()) {
            String pickupKey = "pickup_" + parcel.getParcelId();
            String deliveryKey = "delivery_" + parcel.getParcelId();
            
            int pickupIndex = locationMapping.getLocationIndices().get(pickupKey);
            int deliveryIndex = locationMapping.getLocationIndices().get(deliveryKey);
            
            long pickupNodeIndex = manager.nodeToIndex(pickupIndex);
            long deliveryNodeIndex = manager.nodeToIndex(deliveryIndex);
            
            routing.addPickupAndDelivery(pickupNodeIndex, deliveryNodeIndex);
            
            routing.solver().addConstraint(
                    routing.solver().makeEquality(
                            routing.vehicleVar(pickupNodeIndex),
                            routing.vehicleVar(deliveryNodeIndex)
                    )
            );
            
            routing.solver().addConstraint(
                    routing.solver().makeLessOrEqual(
                            timeDimension.cumulVar(pickupNodeIndex),
                            timeDimension.cumulVar(deliveryNodeIndex)
                    )
            );
        }

    }

    private long[] buildDemandsArray(VrpRequestDto request, LocationMapper.LocationMapping locationMapping) {
        final long[] demands = new long[locationMapping.getLocations().size()];
        Arrays.fill(demands, 0);

        Map<String, Double> parcelDemands = new HashMap<>();
        for (ParcelInfo parcel : request.getParcels()) {
            parcelDemands.put(parcel.getParcelId(), parcel.getVolume());
        }

        for (ParcelInfo parcel : request.getParcels()) {
            String pickupKey = "pickup_" + parcel.getParcelId();
            String deliveryKey = "delivery_" + parcel.getParcelId();
            
            int pickupIndex = locationMapping.getLocationIndices().get(pickupKey);
            int deliveryIndex = locationMapping.getLocationIndices().get(deliveryKey);
            
            long demand = Math.round(parcel.getVolume());
            demands[pickupIndex] = demand;
            demands[deliveryIndex] = -demand;
        }

        return demands;
    }
}
