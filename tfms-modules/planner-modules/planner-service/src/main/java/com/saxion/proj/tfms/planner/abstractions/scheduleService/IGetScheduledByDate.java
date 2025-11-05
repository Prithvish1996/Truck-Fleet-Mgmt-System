package com.saxion.proj.tfms.planner.abstractions.scheduleService;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;

import java.time.ZonedDateTime;
import java.util.List;

public interface IGetScheduledByDate {
    ApiResponse<List<ParcelResponseDto>> Handle(ZonedDateTime plannedDate);
}
