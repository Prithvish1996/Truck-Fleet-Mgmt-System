package com.saxion.proj.tfms.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parcel request payload")
public class ParcelRequestDto {
    @NotBlank(message = "Parcel name is required")
    private String name;

    @NotNull(message = "Warehouse information is required")
    private WareHouseRequestDto warehouse;

    @NotNull(message = "Delivery location is required")
    private LocationRequestDto deliveryLocation;

    private Double weight;

    private Double volume;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Recipient phone is required")
    private String recipientPhone;

    private String deliveryInstructions;
}
