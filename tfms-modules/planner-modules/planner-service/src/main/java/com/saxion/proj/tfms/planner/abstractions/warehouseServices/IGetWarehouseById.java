package com.saxion.proj.tfms.planner.abstractions.WarehouseServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IGetWarehouseById {
    ApiResponse<WareHouseResponseDto> Handle(Long warehouseId);
}
