package com.saxion.proj.tfms.planner.abstractions.scheduleService;

import com.saxion.proj.tfms.commons.dto.ApiResponse;

import java.time.ZonedDateTime;
import java.util.Map;

public interface IGetScheduledDeliveries {
    ApiResponse<Map<String, Object>> Handle(ZonedDateTime plannedDate, int page, int size);
}
