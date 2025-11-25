package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverSuggestionResponseDto {

    private Long id;
    private String suggestions;
    private ZonedDateTime created_at;
}
