package com.saxion.proj.tfms.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckInfo {
    private String truckName;
    private double volume;
}
