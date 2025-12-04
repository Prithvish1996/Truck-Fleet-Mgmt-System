package com.saxion.proj.tfms.routing.model.builder;

import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.model.TruckRouteInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent builder for TruckRouteInfo.
 * Ensures consistent route information construction.
 */
public class TruckRouteInfoBuilder {
    
    private String truckPlateNumber;
    private Long depotId;
    private String depotName;
    private List<Stop> routeStops = new ArrayList<>();
    private int totalDistance = 0;
    private long totalTransportTime = 0L;

    public TruckRouteInfoBuilder forTruck(String plateNumber) {
        this.truckPlateNumber = Objects.requireNonNull(plateNumber, "Plate number cannot be null");
        return this;
    }

    public TruckRouteInfoBuilder fromDepot(Long depotId, String depotName) {
        this.depotId = Objects.requireNonNull(depotId, "Depot ID cannot be null");
        this.depotName = Objects.requireNonNull(depotName, "Depot name cannot be null");
        return this;
    }

    public TruckRouteInfoBuilder withStops(List<Stop> stops) {
        this.routeStops = new ArrayList<>(stops);
        return this;
    }

    public TruckRouteInfoBuilder addStop(Stop stop) {
        this.routeStops.add(Objects.requireNonNull(stop, "Stop cannot be null"));
        return this;
    }

    public TruckRouteInfoBuilder withDistance(int distance) {
        this.totalDistance = distance;
        return this;
    }

    public TruckRouteInfoBuilder withTransportTime(long timeInSeconds) {
        this.totalTransportTime = timeInSeconds;
        return this;
    }

    public TruckRouteInfo build() {
        Objects.requireNonNull(truckPlateNumber, "Truck plate number is required");
        Objects.requireNonNull(depotId, "Depot ID is required");
        Objects.requireNonNull(depotName, "Depot name is required");
        
        if (routeStops.isEmpty()) {
            throw new IllegalArgumentException("Route must contain at least one stop");
        }

        return TruckRouteInfo.builder()
                .truckPlateNumber(truckPlateNumber)
                .depotId(depotId)
                .depotName(depotName)
                .routeStops(routeStops)
                .totalDistance(totalDistance)
                .totalTransportTime(totalTransportTime)
                .build();
    }
}
