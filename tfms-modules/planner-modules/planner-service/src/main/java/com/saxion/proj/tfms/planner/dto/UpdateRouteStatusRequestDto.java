package com.saxion.proj.tfms.planner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRouteStatusRequestDto {
    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotEmpty(message = "Status is required")
    private String status;
}
