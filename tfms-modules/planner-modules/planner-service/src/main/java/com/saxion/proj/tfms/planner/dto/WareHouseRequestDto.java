package com.saxion.proj.tfms.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Warehouse request payload")
public class WareHouseRequestDto {
    @NotBlank(message = "Warehouse name is required")
    private String name;

    @NotNull(message = "Warehouse location is required")
    private LocationRequestDto location;
}
