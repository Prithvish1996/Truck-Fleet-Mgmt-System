package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannerRouteDto {
    private String assignmentId;
    private String truckPlateId;
    private String driverName;
    private String polyline;
    private List<RouteStopDto> stops;
}

