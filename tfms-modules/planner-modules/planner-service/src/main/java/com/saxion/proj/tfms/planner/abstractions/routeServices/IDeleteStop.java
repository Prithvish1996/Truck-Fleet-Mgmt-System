package com.saxion.proj.tfms.planner.abstractions.routeServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;

public interface IDeleteStop {
    ApiResponse<Void> handle(Long stopId);
}
