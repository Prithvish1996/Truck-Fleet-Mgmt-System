package com.saxion.proj.tfms.planner.abstractions.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;

public interface IAssignDriverToTruck {
    ApiResponse<String> Handle(Long driverId, Long truckId);
}
