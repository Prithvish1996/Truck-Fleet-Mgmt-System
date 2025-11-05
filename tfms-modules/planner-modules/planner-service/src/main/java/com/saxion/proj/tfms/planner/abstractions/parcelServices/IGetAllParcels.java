package com.saxion.proj.tfms.planner.abstractions.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import java.util.List;
import java.util.Map;

public interface IGetAllParcels {
    ApiResponse<Map<String, Object>> Handle(Long warehouseId, String searchText, int page, int size);
}

