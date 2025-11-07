package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopDto {
    private int order;
    private String parcelId;
    private String receiver;
    private String address;
    private double latitude;
    private double longitude;
    private String eta;
}

