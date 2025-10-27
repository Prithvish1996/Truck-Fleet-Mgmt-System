package com.saxion.proj.tfms.commons.utility.truckassignment.response;

import com.saxion.proj.tfms.commons.utility.truckassignment.model.TruckAssignment;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;
import java.util.Collections;

public class AssignmentResponse {
    private final boolean success;
    private final String errorMessage;
    private final String errorCode;
    private final List<TruckAssignment> truckAssignments;
    private final int totalParcels;
    private final int trucksUsed;
    private final int assignedParcels;
    private final List<Pair<String, Double>> unassignedParcels;
    private final double totalCapacityUsed;
    private final double totalCapacityAvailable;
    private final double utilizationPercentage;

    // Private constructor for success (all parcels assigned)
    private AssignmentResponse(List<TruckAssignment> truckAssignments, int totalParcels, int trucksUsed) {
        this.success = true;
        this.errorMessage = null;
        this.errorCode = null;
        this.truckAssignments = truckAssignments;
        this.totalParcels = totalParcels;
        this.trucksUsed = trucksUsed;
        this.assignedParcels = totalParcels;
        this.unassignedParcels = Collections.emptyList();
        
        // Calculate space utilization
        this.totalCapacityUsed = calculateTotalCapacityUsed(truckAssignments);
        this.totalCapacityAvailable = calculateTotalCapacityAvailable(truckAssignments);
        this.utilizationPercentage = totalCapacityAvailable > 0 
            ? (totalCapacityUsed / totalCapacityAvailable) * 100.0 
            : 0.0;
    }

    // Private constructor for partial success (some parcels unassigned)
    private AssignmentResponse(List<TruckAssignment> truckAssignments, int totalParcels, int trucksUsed, 
                              List<Pair<String, Double>> unassignedParcels) {
        this.success = true; // Still success, but with warnings
        this.errorMessage = null;
        this.errorCode = "PARTIAL_ASSIGNMENT";
        this.truckAssignments = truckAssignments;
        this.totalParcels = totalParcels;
        this.trucksUsed = trucksUsed;
        this.assignedParcels = totalParcels - unassignedParcels.size();
        this.unassignedParcels = unassignedParcels;
        
        // Calculate space utilization
        this.totalCapacityUsed = calculateTotalCapacityUsed(truckAssignments);
        this.totalCapacityAvailable = calculateTotalCapacityAvailable(truckAssignments);
        this.utilizationPercentage = totalCapacityAvailable > 0 
            ? (totalCapacityUsed / totalCapacityAvailable) * 100.0 
            : 0.0;
    }

    // Private constructor for error (validation failure)
    private AssignmentResponse(String errorCode, String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.truckAssignments = Collections.emptyList();
        this.totalParcels = 0;
        this.trucksUsed = 0;
        this.assignedParcels = 0;
        this.unassignedParcels = Collections.emptyList();
        this.totalCapacityUsed = 0.0;
        this.totalCapacityAvailable = 0.0;
        this.utilizationPercentage = 0.0;
    }

    // Factory method for complete success
    public static AssignmentResponse success(List<TruckAssignment> truckAssignments, int totalParcels, int trucksUsed) {
        return new AssignmentResponse(truckAssignments, totalParcels, trucksUsed);
    }

    // Factory method for partial success (some parcels couldn't be assigned)
    public static AssignmentResponse partialSuccess(List<TruckAssignment> truckAssignments, int totalParcels, 
                                                   int trucksUsed, List<Pair<String, Double>> unassignedParcels) {
        return new AssignmentResponse(truckAssignments, totalParcels, trucksUsed, unassignedParcels);
    }

    // Factory method for error
    public static AssignmentResponse error(String errorCode, String errorMessage) {
        return new AssignmentResponse(errorCode, errorMessage);
    }

    private static double calculateTotalCapacityUsed(List<TruckAssignment> assignments) {
        return assignments.stream()
            .mapToDouble(TruckAssignment::getTotalVolume)
            .sum();
    }

    private static double calculateTotalCapacityAvailable(List<TruckAssignment> assignments) {
        return assignments.stream()
            .mapToDouble(TruckAssignment::getTruckCapacity)
            .sum();
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean hasUnassignedParcels() {
        return !unassignedParcels.isEmpty();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<TruckAssignment> getTruckAssignments() {
        return truckAssignments;
    }

    public int getTotalParcels() {
        return totalParcels;
    }

    public int getTrucksUsed() {
        return trucksUsed;
    }

    public int getAssignedParcels() {
        return assignedParcels;
    }

    public List<Pair<String, Double>> getUnassignedParcels() {
        return unassignedParcels;
    }

    public double getTotalCapacityUsed() {
        return totalCapacityUsed;
    }

    public double getTotalCapacityAvailable() {
        return totalCapacityAvailable;
    }

    public double getUtilizationPercentage() {
        return utilizationPercentage;
    }

    @Override
    public String toString() {
        if (!success) {
            return String.format("AssignmentResponse{success=false, errorCode='%s', errorMessage='%s'}", 
                errorCode, errorMessage);
        } else if (hasUnassignedParcels()) {
            return String.format("AssignmentResponse{success=true, totalParcels=%d, assignedParcels=%d, " +
                "unassignedParcels=%d, trucksUsed=%d, capacityUsed=%.2f, utilization=%.1f%%}", 
                totalParcels, assignedParcels, unassignedParcels.size(), trucksUsed, 
                totalCapacityUsed, utilizationPercentage);
        } else {
            return String.format("AssignmentResponse{success=true, totalParcels=%d, trucksUsed=%d, " +
                "capacityUsed=%.2f, utilization=%.1f%%}", 
                totalParcels, trucksUsed, totalCapacityUsed, utilizationPercentage);
        }
    }
}