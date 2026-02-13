package com.saxion.proj.tfms.planner.abstractions.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.UpdateParcelStatusRequestDto;

public interface IUpdateParcelStatus {
    ApiResponse<String> handle(UpdateParcelStatusRequestDto dto);
}
