package com.saxion.proj.tfms.commons.utility.truckassignment.service;

import com.saxion.proj.tfms.commons.utility.truckassignment.model.ParcelInfo;
import com.saxion.proj.tfms.commons.utility.truckassignment.model.TruckAssignment;
import com.saxion.proj.tfms.commons.utility.truckassignment.response.AssignmentResponse;
import org.apache.commons.lang3.tuple.Pair;
import java.util.*;

public class TruckAssignmentServiceImpl implements TruckAssignmentService {

    @Override
    public AssignmentResponse assignParcelsToTrucks(
            List<Pair<String, Double>> trucks,
            List<Pair<String, Double>> parcels) {
        
        // Validate inputs and return error response if validation fails
        AssignmentResponse validationError = validateInput(trucks, parcels);
        if (validationError != null) {
            return validationError;
        }

        try {
            // Use a map to track assignments per truck (fixes duplicate truck issue)
            Map<String, TruckState> truckStates = new LinkedHashMap<>();
            
            // Initialize truck states
            for (Pair<String, Double> truck : trucks) {
                String truckId = truck.getKey();
                double capacity = truck.getValue();
                truckStates.put(truckId, new TruckState(truckId, capacity));
            }

            List<Pair<String, Double>> unassignedParcels = new ArrayList<>();

            // Assign parcels using best-fit-decreasing algorithm for better space utilization
            for (Pair<String, Double> parcel : parcels) {
                String parcelId = parcel.getKey();
                double volume = parcel.getValue();

                // Find the best truck for this parcel (smallest remaining space that fits)
                TruckState bestTruck = findBestFitTruck(truckStates.values(), volume);

                if (bestTruck != null) {
                    // Assign parcel to the best truck
                    bestTruck.addParcel(new ParcelInfo(parcelId, volume));
                } else {
                    // No truck can fit this parcel
                    unassignedParcels.add(parcel);
                }
            }

            // Convert truck states to assignments (only trucks with parcels)
            List<TruckAssignment> assignments = new ArrayList<>();
            int trucksUsed = 0;
            
            for (TruckState state : truckStates.values()) {
                if (!state.getParcels().isEmpty()) {
                    assignments.add(new TruckAssignment(
                        state.getTruckId(),
                        state.getParcels(),
                        state.getUsedCapacity(),
                        state.getTotalCapacity()  // Now passing total capacity
                    ));
                    trucksUsed++;
                }
            }

            // Return appropriate response
            if (unassignedParcels.isEmpty()) {
                return AssignmentResponse.success(assignments, parcels.size(), trucksUsed);
            } else {
                return AssignmentResponse.partialSuccess(assignments, parcels.size(), trucksUsed, unassignedParcels);
            }
            
        } catch (Exception e) {
            return AssignmentResponse.error("INTERNAL_ERROR", 
                "An unexpected error occurred during assignment: " + e.getMessage());
        }
    }

    /**
     * Find the best truck for a parcel using best-fit algorithm
     * Returns truck with smallest remaining space that can fit the parcel
     * This minimizes wasted space and allows backtracking to previous trucks
     */
    private TruckState findBestFitTruck(Collection<TruckState> trucks, double parcelVolume) {
        TruckState bestTruck = null;
        double smallestRemainingSpace = Double.MAX_VALUE;

        for (TruckState truck : trucks) {
            double remainingSpace = truck.getRemainingCapacity();
            
            // Check if parcel fits in this truck
            if (parcelVolume <= remainingSpace) {
                // Check if this truck is better than current best
                // (has smaller remaining space after assignment)
                double wastedSpace = remainingSpace - parcelVolume;
                
                if (wastedSpace < smallestRemainingSpace) {
                    bestTruck = truck;
                    smallestRemainingSpace = wastedSpace;
                }
            }
        }

        return bestTruck;
    }

    /**
     * Internal class to track truck state during assignment
     */
    private static class TruckState {
        private final String truckId;
        private final double totalCapacity;
        private final List<ParcelInfo> parcels;
        private double usedCapacity;

        public TruckState(String truckId, double totalCapacity) {
            this.truckId = truckId;
            this.totalCapacity = totalCapacity;
            this.parcels = new ArrayList<>();
            this.usedCapacity = 0.0;
        }

        public void addParcel(ParcelInfo parcel) {
            parcels.add(parcel);
            usedCapacity += parcel.getVolume();
        }

        public double getRemainingCapacity() {
            return totalCapacity - usedCapacity;
        }

        public String getTruckId() {
            return truckId;
        }

        public double getTotalCapacity() {
            return totalCapacity;
        }

        public List<ParcelInfo> getParcels() {
            return parcels;
        }

        public double getUsedCapacity() {
            return usedCapacity;
        }

        public double getUtilizationPercentage() {
            return (usedCapacity / totalCapacity) * 100.0;
        }
    }

    private AssignmentResponse validateInput(List<Pair<String, Double>> trucks, List<Pair<String, Double>> parcels) {
        if (trucks == null || trucks.isEmpty()) {
            return AssignmentResponse.error("INVALID_INPUT", "Trucks list cannot be null or empty");
        }
        if (parcels == null || parcels.isEmpty()) {
            return AssignmentResponse.error("INVALID_INPUT", "Parcels list cannot be null or empty");
        }

        // Check for invalid truck capacities
        for (Pair<String, Double> truck : trucks) {
            if (truck.getValue() == null || truck.getValue() <= 0) {
                return AssignmentResponse.error("INVALID_TRUCK_CAPACITY",
                    String.format("Truck %s has invalid capacity: %.2f", truck.getKey(), truck.getValue()));
            }
        }

        // Check for invalid parcel volumes
        for (Pair<String, Double> parcel : parcels) {
            if (parcel.getValue() == null || parcel.getValue() <= 0) {
                return AssignmentResponse.error("INVALID_PARCEL_VOLUME",
                    String.format("Parcel %s has invalid volume: %.2f", parcel.getKey(), parcel.getValue()));
            }
        }

        // Note: We no longer check total capacity or max truck capacity
        // Instead, we'll assign as many parcels as possible and report unassigned ones

        return null; // No validation errors
    }
}
