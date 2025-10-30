package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.abstractions.ScheduleService.IGetScheduledByDate;
import com.saxion.proj.tfms.planner.abstractions.ScheduleService.IGetScheduledDeliveries;
import com.saxion.proj.tfms.planner.abstractions.ScheduleService.IScheduleNextDayDelivery;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.dto.ScheduleRequestDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/planner/schedule")
public class ScheduleController {

    @Autowired
    private IScheduleNextDayDelivery scheduleNextDayDelivery;

    @Autowired
    private IGetScheduledDeliveries getNextDayDelivery;

    @Autowired
    private IGetScheduledByDate getScheduleDeliveries;

    //schedule parcel delivery
    @PostMapping("/schedule-next-day")
    public ApiResponse<List<ParcelResponseDto>> scheduleNextDay(
            @Valid @CurrentUser UserContext user,
            @RequestBody ScheduleRequestDto dto
    ){
        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        if (!Objects.equals(user.getRole(), "PLANNER")) {
            return ApiResponse.error("Not Authorized");
        }

        return scheduleNextDayDelivery.Handle(dto);
    }

    // List all schedule delivery with pagination
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSchedules(
            @CurrentUser UserContext user,
            @RequestParam(required = false) ZonedDateTime date,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if(!Objects.equals(role, "PLANNER")){
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(getNextDayDelivery.Handle(date, page, size));
    }

    // List all schedule delivery without pagination
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ParcelResponseDto>>> getAllSchedulesWithoutPagination(
            @CurrentUser UserContext user,
            @RequestParam(required = false) ZonedDateTime date) {

        // Authentication check
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        // Authorization check
        if (!"PLANNER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }

        ApiResponse<List<ParcelResponseDto>> response = getScheduleDeliveries.Handle(date);
        return ResponseEntity.ok(response);
    }
}
