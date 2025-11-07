package com.saxion.proj.tfms.routing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckInfo {
    private Long truckId;
    private String truckName;
    private double volume;


}
