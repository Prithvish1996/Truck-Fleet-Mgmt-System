package com.saxion.proj.tfms.planner.abstractions.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;

import java.util.List;

public interface ICreateParcelsBulk {
    ApiResponse<List<ParcelResponseDto>> Handle(List<ParcelRequestDto> parcelDtos);
}
