package com.saxion.proj.tfms.planner.abstractions.routeServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.AssignRouteRequestDto;

public interface IAssignDriverToRoute {
    ApiResponse<String> handle(AssignRouteRequestDto dto);
}
