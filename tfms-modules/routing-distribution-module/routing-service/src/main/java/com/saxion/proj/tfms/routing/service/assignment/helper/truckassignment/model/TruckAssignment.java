package com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model;

import java.util.List;

/**
 * Truck assignment details
 */
public class TruckAssignment {
    private final String truckId;
    private final List<ParcelInfo> parcels;
    private final double totalVolume; // Used capacity
    private final double truckCapacity; // Total truck capacity

    public TruckAssignment(String truckId, List<ParcelInfo> parcels, double totalVolume) {
        this.truckId = truckId;
        this.parcels = parcels;
        this.totalVolume = totalVolume;
        this.truckCapacity = totalVolume; // For backward compatibility
    }

    public TruckAssignment(String truckId, List<ParcelInfo> parcels, double totalVolume, double truckCapacity) {
        this.truckId = truckId;
        this.parcels = parcels;
        this.totalVolume = totalVolume;
        this.truckCapacity = truckCapacity;
    }

    public String getTruckId() {
        return truckId;
    }

    public List<ParcelInfo> getParcels() {
        return parcels;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public double getTruckCapacity() {
        return truckCapacity;
    }

    public double getRemainingCapacity() {
        return truckCapacity - totalVolume;
    }

    public double getUtilizationPercentage() {
        return truckCapacity > 0 ? (totalVolume / truckCapacity) * 100.0 : 0.0;
    }

    public int getParcelCount() {
        return parcels.size();
    }

    /**
     * Parcel information
     */
    public static class ParcelInfo {
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
}
