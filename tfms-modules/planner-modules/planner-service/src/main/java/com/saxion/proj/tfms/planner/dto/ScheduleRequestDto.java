package com.saxion.proj.tfms.planner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {

    @NotNull
    private LocalDate deliveryDate;

    @NotEmpty
    private String truckPlateId;

    @NotEmpty
    private String priority;

    @NotEmpty
    private List<String> parcelIds;
}

