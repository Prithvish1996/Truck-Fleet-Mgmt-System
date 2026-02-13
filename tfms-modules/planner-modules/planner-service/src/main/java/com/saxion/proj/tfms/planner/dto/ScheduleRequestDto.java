package com.saxion.proj.tfms.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schedule request payload")
public class ScheduleRequestDto {
    @NotBlank(message = "Select at least 1 parcel to be schedule.")
    private List<Long> parcelIds;

    private ZonedDateTime deliveryDate; // if empty the system default to next day date
}


