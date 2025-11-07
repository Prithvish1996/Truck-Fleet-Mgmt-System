package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckSummaryDto {
    private String plateId;
    private String warehouse;
    private int capacity;
    private String status;
}

