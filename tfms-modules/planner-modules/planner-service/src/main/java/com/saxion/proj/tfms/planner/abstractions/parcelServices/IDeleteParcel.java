package com.saxion.proj.tfms.planner.abstractions.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;

public interface IDeleteParcel {
    ApiResponse<Void> Handle(Long parcelId);
}
