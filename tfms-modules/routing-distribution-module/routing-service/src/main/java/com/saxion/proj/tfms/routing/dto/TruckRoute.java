package com.saxion.proj.tfms.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckRoute {
    private String truckId;
    private int distance;
    private int transportTime;
    private List<Activity> activities;
    private List<String> assignedParcels;
    private String warehouseVisited;
}
