package com.saxion.proj.tfms.planner.abstractions.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;

public interface IGetParcelById {
    ApiResponse<ParcelResponseDto> Handle(Long parcelId);
}
