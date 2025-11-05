package com.saxion.proj.tfms.planner.abstractions.truckServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.TruckResponseDto;

public interface IGetTruckById
{
    ApiResponse<TruckResponseDto> Handle(Long truckId);
}
