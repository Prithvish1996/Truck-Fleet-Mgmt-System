package com.saxion.proj.tfms.planner.dto.routing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parcel {
    private Long parcelId;
    private String parcelName;
    private double volume;

    // Warehouse (pickup) location
    private Long warehouseId;
    private double warehouseLatitude;
    private double warehouseLongitude;

    // Customer (delivery) location
    private double deliveryLatitude;
    private double deliveryLongitude;
    private String recipientName;
    private String recipientPhone;
    private String deliveryInstructions;

    public static Parcel findByParcelName(List<Parcel> parcels, String name) {
        if (parcels == null || name == null) return null;

        for (Parcel parcel : parcels) {
            if (name.equals(parcel.getParcelName())) {
                return parcel;
            }
        }
        return null;
    }
}
