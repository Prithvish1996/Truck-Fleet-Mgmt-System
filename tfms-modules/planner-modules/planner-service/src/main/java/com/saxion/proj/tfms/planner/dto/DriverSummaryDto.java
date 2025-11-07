package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverSummaryDto {
    private String id;
    private String name;
    private String email;
    private List<String> licenses;
    private String workWindowStart;
    private String workWindowEnd;
}

