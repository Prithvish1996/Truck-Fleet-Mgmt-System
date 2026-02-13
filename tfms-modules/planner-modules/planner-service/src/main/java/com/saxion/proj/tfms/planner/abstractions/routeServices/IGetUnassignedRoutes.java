package com.saxion.proj.tfms.planner.abstractions.routeServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.GenerateRouteResponseDto;

public interface IGetUnassignedRoutes {
    ApiResponse<GenerateRouteResponseDto> handle();
}
