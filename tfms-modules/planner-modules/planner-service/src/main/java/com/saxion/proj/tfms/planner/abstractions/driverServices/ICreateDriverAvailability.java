package com.saxion.proj.tfms.planner.abstractions.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DriverAvailabilityRequestDto;

import java.util.List;


public interface ICreateDriverAvailability {
    ApiResponse<String> Handle(Long driverId, List<DriverAvailabilityRequestDto> dates) ;
}
