package com.saxion.proj.tfms.planner.abstractions.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;

public interface IUpdateParcel {
    ApiResponse<ParcelResponseDto> Handle(Long parcelId, ParcelRequestDto dto);
}
