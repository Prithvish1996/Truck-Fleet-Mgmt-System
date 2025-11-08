package com.saxion.proj.tfms.planner.abstractions.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;

public interface IUpdateDriverAvailability {
    ApiResponse<DriverResponseDto> Handle(Long driverId, boolean isAvailable);
}
