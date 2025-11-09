package com.saxion.proj.tfms.planner.dto.routing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VRPResponse {
    List<WarehouseRoutingResult> warehouseRoutingResults;
    private int totalTrucksUsed;
    private double esitimatedDistanceInkm;
    private long estimatedTimeInMinutes;
    private String notes;
}
