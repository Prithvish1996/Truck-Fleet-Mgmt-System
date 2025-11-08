package com.saxion.proj.tfms.planner.dto.routing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckRouteInfo {

    // Represents a single route for a truck.

    private String truckPlateNumber;
    private Long depotId;
    private String depotName;
    private List<Stop> routeStops;
    private Long totalDistance;
    private Long totalTransportTime;
}