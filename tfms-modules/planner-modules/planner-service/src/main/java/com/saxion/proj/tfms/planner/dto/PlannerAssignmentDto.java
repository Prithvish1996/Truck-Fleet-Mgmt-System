package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannerAssignmentDto {
    private String assignmentId;
    private String truckPlateId;
    private LocalDateTime date;
    private int parcelCount;
    private String driverId;
    private String driverName;
    private List<String> requestIds;
    private String status;
}

