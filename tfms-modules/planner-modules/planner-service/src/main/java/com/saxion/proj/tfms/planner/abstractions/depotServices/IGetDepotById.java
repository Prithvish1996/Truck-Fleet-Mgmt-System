package com.saxion.proj.tfms.planner.abstractions.depotServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;

public interface IGetDepotById {
    ApiResponse<DepotResponseDto> Handle(Long depotId);
}
