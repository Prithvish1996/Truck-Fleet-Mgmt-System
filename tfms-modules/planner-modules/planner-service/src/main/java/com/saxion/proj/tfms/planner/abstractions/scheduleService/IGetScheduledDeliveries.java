package com.saxion.proj.tfms.planner.abstractions.ScheduleService;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.dto.ScheduleRequestDto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface IGetScheduledDeliveries {
    ApiResponse<Map<String, Object>> Handle(ZonedDateTime plannedDate, int page, int size);
}
