package com.saxion.proj.tfms.planner.abstractions.routeServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.RouteResponseDto;

public interface IGetRouteById {
    ApiResponse<RouteResponseDto> handle(Long id);
}
