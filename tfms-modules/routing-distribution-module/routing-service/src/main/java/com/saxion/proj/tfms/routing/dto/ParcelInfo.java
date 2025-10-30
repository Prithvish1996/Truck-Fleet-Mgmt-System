package com.saxion.proj.tfms.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelInfo {
    private String parcelId;
    private String parcelName;
    private double volume;

    // Warehouse (pickup) location
    private String warehouseId;
    private double warehouseLatitude;
    private double warehouseLongitude;

    // Customer (delivery) location
    private double deliveryLatitude;
    private double deliveryLongitude;
    private String recipientName;
}
