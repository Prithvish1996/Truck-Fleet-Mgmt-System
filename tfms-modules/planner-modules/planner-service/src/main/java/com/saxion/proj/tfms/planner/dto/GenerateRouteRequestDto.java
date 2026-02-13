package com.saxion.proj.tfms.planner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerateRouteRequestDto {
    @NotNull(message = "Depot ID is required")
    private Long depot_id;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouse_id;

    @NotEmpty(message = "Parcel IDs are required")
    private List<Long> parcelIds;
}
