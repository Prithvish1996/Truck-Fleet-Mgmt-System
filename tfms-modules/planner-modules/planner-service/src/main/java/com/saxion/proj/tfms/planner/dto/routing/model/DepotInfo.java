package com.saxion.proj.tfms.planner.dto.routing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepotInfo {
    private Long depotId;
    private String depotName;
    private double latitude;
    private double longitude;
}