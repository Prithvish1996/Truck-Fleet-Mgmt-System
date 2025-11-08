package com.saxion.proj.tfms.planner.abstractions.routeServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.UpdateRouteStatusRequestDto;

public interface IUpdateRouteStatus {
    ApiResponse<String> handle(UpdateRouteStatusRequestDto dto);
}
