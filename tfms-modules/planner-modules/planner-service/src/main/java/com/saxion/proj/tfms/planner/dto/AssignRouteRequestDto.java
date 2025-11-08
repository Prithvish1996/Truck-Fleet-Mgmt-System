package com.saxion.proj.tfms.planner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
@Data
public class AssignRouteRequestDto {
    @NotNull(message = "Route is required")
    private Long routId;

    @NotNull(message = "Truck selection is required")
    private Long truckId;

    @NotNull(message = "Driver selection is required")
    private Long driverId;

    private List<StopDto> stops;
}
