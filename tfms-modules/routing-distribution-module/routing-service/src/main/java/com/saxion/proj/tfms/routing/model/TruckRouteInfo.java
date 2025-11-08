package com.saxion.proj.tfms.routing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckRouteInfo {

    // Represents a single route for a truck.

    private String truckName;
    private Long depotId;
    private String depotName;
    private List<Stop> routeStops;
    private Integer totalDistance;
    private Long totalTransportTime;
}
