package com.saxion.proj.tfms.planner.abstractions.warehouseServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;

public interface IUpdateWarehouse {
    ApiResponse<WareHouseResponseDto> Handle(Long warehouseId, WareHouseRequestDto dto);
}
