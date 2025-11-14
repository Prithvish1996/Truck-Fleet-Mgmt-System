package com.saxion.proj.tfms.planner.abstractions.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;

public interface ICreateDriverSuggestion {
    ApiResponse<String> Handle(Long driverId, String suggestion);
}
