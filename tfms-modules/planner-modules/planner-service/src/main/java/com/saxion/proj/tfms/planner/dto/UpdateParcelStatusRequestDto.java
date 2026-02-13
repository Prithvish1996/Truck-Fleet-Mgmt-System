package com.saxion.proj.tfms.planner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateParcelStatusRequestDto {
    @NotNull(message = "Parcel ID is required.")
    private Long parcelId;

    @NotEmpty(message = "Status is required")
    private String status;
}
