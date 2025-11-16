package com.saxion.proj.tfms.planner.abstractions.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DriverAvailabilityResponseDto;

import java.time.ZonedDateTime;
import java.util.List;

public interface ICreateDriverAvailability {
    ApiResponse<String> Handle(Long driverId, List<ZonedDateTime> dates);
}
