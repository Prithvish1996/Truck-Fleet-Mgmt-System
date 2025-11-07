package com.saxion.proj.tfms.planner.dto;

import lombok.Data;

import java.util.List;

@Data
public class DriverRouteResponseDto {
    private List<RouteResponseDto> routes;
}