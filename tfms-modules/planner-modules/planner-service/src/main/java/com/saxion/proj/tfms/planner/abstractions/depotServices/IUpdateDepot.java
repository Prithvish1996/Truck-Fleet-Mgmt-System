package com.saxion.proj.tfms.planner.abstractions.depotServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DepotRequestDto;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;

public interface IUpdateDepot {
    ApiResponse<DepotResponseDto> Handle(Long depotId, DepotRequestDto dto);
}
