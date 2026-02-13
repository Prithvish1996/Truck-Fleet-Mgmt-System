package com.saxion.proj.tfms.planner.abstractions.routeServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.GenerateRouteRequestDto;
import com.saxion.proj.tfms.planner.dto.GenerateRouteResponseDto;

public interface ICreateRoute {
    ApiResponse<GenerateRouteResponseDto> handle(GenerateRouteRequestDto request) ;
}
