package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAvailabilityResponseDto {

    private Long id;
    private ZonedDateTime availableAt;
    private String startTime;
    private String endTime;
    private String status;
}
