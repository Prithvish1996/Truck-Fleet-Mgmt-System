package com.saxion.proj.tfms.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Depot request payload")
public class DepotRequestDto {
    @NotBlank(message = "Depot name is required")
    private String name;

    @NotNull(message = "Depot location is required")
    private LocationRequestDto location;
}
