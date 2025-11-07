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
public class PlannerRequestDto {
    private String requestId;
    private String truckPlateId;
    private LocalDateTime deliveryDate;
    private int parcelCount;
    private String warehouse;
    private String priority;
    private List<String> parcelIds;
}

