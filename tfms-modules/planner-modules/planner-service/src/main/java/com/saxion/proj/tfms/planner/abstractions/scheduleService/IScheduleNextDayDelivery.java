package com.saxion.proj.tfms.planner.abstractions.scheduleService;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.dto.ScheduleRequestDto;

import java.util.List;

public interface IScheduleNextDayDelivery {
    ApiResponse<List<ParcelResponseDto>> Handle(ScheduleRequestDto request);
}
