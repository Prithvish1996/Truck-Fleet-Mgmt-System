package com.saxion.proj.tfms.planner.abstractions.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;

import java.util.Map;

public interface IGetAllParcels {
    ApiResponse<Map<String, Object>> Handle(Long warehouseId, String searchText, int page, int size);
}

