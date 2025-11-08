package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRouteResponseDto {
    private List<RouteResponseDto> assignRoutes;
    private List<RouteResponseDto> unAssignedRoute;
    private List<TruckResponseDto> trucks;
    private List<DriverResponseDto> drivers;
}
