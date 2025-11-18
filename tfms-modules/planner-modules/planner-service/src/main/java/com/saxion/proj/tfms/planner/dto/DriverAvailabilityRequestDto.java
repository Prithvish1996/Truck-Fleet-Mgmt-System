package com.saxion.proj.tfms.planner.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAvailabilityRequestDto {
    /**
     * Valid date only — no time allowed.
     * Accepts format: yyyy-MM-dd
     */
    @NotNull(message = "Available date is required.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate availableAt;

    /**
     * Valid time only (HH:mm)
     */
    @NotNull(message = "Start time is required.")
    @Pattern(
            regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
            message = "Start time must be in HH:mm format (00:00–23:59)."
    )
    private String startTime;

    /**
     * Valid time only (HH:mm)
     */
    @NotNull(message = "End time is required.")
    @Pattern(
            regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
            message = "End time must be in HH:mm format (00:00–23:59)."
    )
    private String endTime;
}
