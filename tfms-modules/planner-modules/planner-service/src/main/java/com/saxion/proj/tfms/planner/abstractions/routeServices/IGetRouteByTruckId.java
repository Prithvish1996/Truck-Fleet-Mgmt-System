package com.saxion.proj.tfms.planner.abstractions.routeServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DriverRouteResponseDto;

public interface IGetRouteByTruckId {
    ApiResponse<DriverRouteResponseDto> handle(Long truckId);
}
