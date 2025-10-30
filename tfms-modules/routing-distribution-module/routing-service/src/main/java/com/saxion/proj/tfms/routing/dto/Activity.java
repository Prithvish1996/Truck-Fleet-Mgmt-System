package com.saxion.proj.tfms.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private String type;
    private String id;
    private String locationId;
    private double latitude;
    private double longitude;
    private int arrivalTime;
    private int endTime;
    private int distance;
    private int drivingTime;
    private List<Integer> loadAfter;
}
