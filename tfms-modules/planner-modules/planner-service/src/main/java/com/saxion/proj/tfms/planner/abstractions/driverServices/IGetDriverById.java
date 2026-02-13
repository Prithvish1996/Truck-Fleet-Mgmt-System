package com.saxion.proj.tfms.planner.abstractions.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;

public interface IGetDriverById {
    ApiResponse<DriverResponseDto> Handle(Long driverId);
}
