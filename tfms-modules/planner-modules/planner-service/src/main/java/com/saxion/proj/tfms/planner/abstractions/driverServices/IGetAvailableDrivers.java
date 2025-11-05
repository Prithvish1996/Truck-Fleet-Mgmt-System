package com.saxion.proj.tfms.planner.abstractions.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;

import java.util.List;

public interface IGetAvailableDrivers {
    ApiResponse<List<DriverResponseDto>> Handle();
}
