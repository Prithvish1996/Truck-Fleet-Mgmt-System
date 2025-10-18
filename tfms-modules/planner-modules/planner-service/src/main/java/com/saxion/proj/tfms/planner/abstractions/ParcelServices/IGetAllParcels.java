package com.saxion.proj.tfms.planner.abstractions.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import java.util.List;

public interface IGetAllParcels {
    ApiResponse<List<ParcelResponseDto>> Handle(Long warehouseId);
}

