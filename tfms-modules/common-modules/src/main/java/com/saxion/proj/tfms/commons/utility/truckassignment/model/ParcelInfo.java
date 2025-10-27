package com.saxion.proj.tfms.commons.utility.truckassignment.model;


/**
 * Parcel information
 */
public class ParcelInfo {
    private final String parcelId;
    private final double volume;

    public ParcelInfo(String parcelId, double volume) {
        this.parcelId = parcelId;
        this.volume = volume;
    }

    public String getParcelId() {
        return parcelId;
    }

    public double getVolume() {
        return volume;
    }
}