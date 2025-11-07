package com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment;

import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.service.TruckAssingmentAlgoService;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.service.TruckAssingmentAlgoServiceAlgoImpl;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * Demo for truck assignment with partial assignment support
 */
public class TruckAssignmentDemo {

    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("   TRUCK ASSIGNMENT DEMO - PARTIAL ASSIGNMENTS");
        System.out.println("================================================\n");

        testSuccessfulAssignment();
        testPartialAssignment();
        testMultipleTrucksPartialAssignment();
        testBestFitAlgorithm();
        testInvalidInput();
        
        System.out.println("\n================================================");
        System.out.println("   ALL TESTS COMPLETED!");
        System.out.println("================================================");
    }

    private static void testSuccessfulAssignment() {
        System.out.println("Test 1: Successful TruckWarehouseAssignment");
        System.out.println("------------------------------");

        TruckAssingmentAlgoService service = new TruckAssingmentAlgoServiceAlgoImpl();

        List<Pair<String, Double>> trucks = Arrays.asList(
            new ImmutablePair<>("TRUCK-001", 100.0),
            new ImmutablePair<>("TRUCK-002", 150.0)
        );

        List<Pair<String, Double>> parcels = Arrays.asList(
            new ImmutablePair<>("PARCEL-001", 50.0),
            new ImmutablePair<>("PARCEL-002", 40.0),
            new ImmutablePair<>("PARCEL-003", 70.0),
            new ImmutablePair<>("PARCEL-004", 60.0)
        );

        AssignmentResponse response = service.assignParcelsToTrucks(trucks, parcels);

        if (response.isSuccess()) {
            System.out.println("✓ SUCCESS - All parcels assigned");
            System.out.println("  Total Parcels: " + response.getTotalParcels());
            System.out.println("  Assigned: " + response.getAssignedParcels());
            System.out.println("  Trucks Used: " + response.getTrucksUsed());
            System.out.println("  Total Capacity Used: " + String.format("%.2f", response.getTotalCapacityUsed()));
            System.out.println("  Overall Utilization: " + String.format("%.1f%%", response.getUtilizationPercentage()));
            System.out.println("\n  Truck Details:");
            for (TruckAssignment assignment : response.getTruckAssignments()) {
                System.out.println("    " + assignment.getTruckId() + ":");
                System.out.println("      Parcels: " + assignment.getParcelCount());
                System.out.println("      Used: " + String.format("%.2f / %.2f", 
                    assignment.getTotalVolume(), assignment.getTruckCapacity()));
                System.out.println("      Utilization: " + String.format("%.1f%%", 
                    assignment.getUtilizationPercentage()));
            }
        } else {
            System.out.println("✗ ERROR: " + response.getErrorMessage());
        }
        System.out.println();
    }

    private static void testPartialAssignment() {
        System.out.println("Test 2: Partial TruckWarehouseAssignment (truck capacity 60, parcels [20, 10, 10, 60])");
        System.out.println("--------------------------------------------------------------------------");

        TruckAssingmentAlgoService service = new TruckAssingmentAlgoServiceAlgoImpl();

        List<Pair<String, Double>> trucks = Arrays.asList(
            new ImmutablePair<>("TRUCK-001", 60.0)
        );

        List<Pair<String, Double>> parcels = Arrays.asList(
            new ImmutablePair<>("PARCEL-001", 20.0),
            new ImmutablePair<>("PARCEL-002", 10.0),
            new ImmutablePair<>("PARCEL-003", 10.0),
            new ImmutablePair<>("PARCEL-004", 60.0)  // This won't fit
        );

        AssignmentResponse response = service.assignParcelsToTrucks(trucks, parcels);

        if (response.isSuccess() && !response.hasUnassignedParcels()) {
            System.out.println("✓ SUCCESS - All parcels assigned");
        } else if (response.hasUnassignedParcels()) {
            System.out.println("⚠ PARTIAL SUCCESS - Some parcels couldn't be assigned");
            System.out.println("  Total Parcels: " + response.getTotalParcels());
            System.out.println("  Assigned: " + response.getAssignedParcels());
            System.out.println("  Unassigned: " + response.getUnassignedParcels().size());
            
            System.out.println("\n  Assigned Parcels:");
            for (TruckAssignment assignment : response.getTruckAssignments()) {
                System.out.println("    " + assignment.getTruckId() + ":");
                for (TruckAssignment.ParcelInfo parcel : assignment.getParcels()) {
                    System.out.println("      - " + parcel.getParcelId() + " (volume: " + parcel.getVolume() + ")");
                }
                System.out.println("      Total: " + String.format("%.2f", assignment.getTotalVolume()));
            }
            
            System.out.println("\n  Unassigned Parcels:");
            for (Pair<String, Double> parcel : response.getUnassignedParcels()) {
                System.out.println("    - " + parcel.getKey() + " (volume: " + parcel.getValue() + ")");
            }
        } else {
            System.out.println("✗ ERROR: " + response.getErrorMessage());
        }
        System.out.println();
    }

    private static void testMultipleTrucksPartialAssignment() {
        System.out.println("Test 3: Multiple Trucks with Partial TruckWarehouseAssignment");
        System.out.println("------------------------------------------------");

        TruckAssingmentAlgoService service = new TruckAssingmentAlgoServiceAlgoImpl();

        List<Pair<String, Double>> trucks = Arrays.asList(
            new ImmutablePair<>("TRUCK-001", 50.0),
            new ImmutablePair<>("TRUCK-002", 40.0)
        );

        List<Pair<String, Double>> parcels = Arrays.asList(
            new ImmutablePair<>("PARCEL-001", 30.0),
            new ImmutablePair<>("PARCEL-002", 25.0),
            new ImmutablePair<>("PARCEL-003", 35.0),
            new ImmutablePair<>("PARCEL-004", 100.0),  // Too large for any truck
            new ImmutablePair<>("PARCEL-005", 15.0)
        );

        AssignmentResponse response = service.assignParcelsToTrucks(trucks, parcels);

        if (response.isSuccess() && !response.hasUnassignedParcels()) {
            System.out.println("✓ SUCCESS - All parcels assigned");
        } else if (response.hasUnassignedParcels()) {
            System.out.println("⚠ PARTIAL SUCCESS - Some parcels couldn't be assigned");
            System.out.println("  Total Parcels: " + response.getTotalParcels());
            System.out.println("  Assigned: " + response.getAssignedParcels());
            System.out.println("  Unassigned: " + response.getUnassignedParcels().size());
            
            System.out.println("\n  Assigned Parcels:");
            for (TruckAssignment assignment : response.getTruckAssignments()) {
                System.out.println("    " + assignment.getTruckId() + ":");
                for (TruckAssignment.ParcelInfo parcel : assignment.getParcels()) {
                    System.out.println("      - " + parcel.getParcelId() + " (volume: " + parcel.getVolume() + ")");
                }
                System.out.println("      Total: " + String.format("%.2f", assignment.getTotalVolume()));
            }
            
            System.out.println("\n  Unassigned Parcels:");
            for (Pair<String, Double> parcel : response.getUnassignedParcels()) {
                System.out.println("    - " + parcel.getKey() + " (volume: " + parcel.getValue() + 
                    ") - too large for any available truck");
            }
        } else {
            System.out.println("✗ ERROR: " + response.getErrorMessage());
        }
        System.out.println();
    }

    private static void testBestFitAlgorithm() {
        System.out.println("Test 4: Best-Fit Algorithm & Space Optimization");
        System.out.println("------------------------------------------------");
        System.out.println("This test demonstrates:");
        System.out.println("  1. Best-fit allocation (minimizes wasted space)");
        System.out.println("  2. Backtracking to previous trucks");
        System.out.println("  3. No duplicate truck assignments");

        TruckAssingmentAlgoService service = new TruckAssingmentAlgoServiceAlgoImpl();

        List<Pair<String, Double>> trucks = Arrays.asList(
            new ImmutablePair<>("TRUCK-A", 100.0),
            new ImmutablePair<>("TRUCK-B", 50.0),
            new ImmutablePair<>("TRUCK-C", 80.0)
        );

        // Parcels that would cause issues with first-fit but work well with best-fit
        List<Pair<String, Double>> parcels = Arrays.asList(
            new ImmutablePair<>("PARCEL-1", 45.0),  // Goes to TRUCK-B (best fit: 50)
            new ImmutablePair<>("PARCEL-2", 75.0),  // Goes to TRUCK-C (best fit: 80)
            new ImmutablePair<>("PARCEL-3", 95.0),  // Goes to TRUCK-A (best fit: 100)
            new ImmutablePair<>("PARCEL-4", 5.0)    // Backtrack to TRUCK-B (5 remaining)
        );

        AssignmentResponse response = service.assignParcelsToTrucks(trucks, parcels);

        if (response.isSuccess() && !response.hasUnassignedParcels()) {
            System.out.println("\n✓ SUCCESS - All parcels optimally assigned");
            System.out.println("  Total Parcels: " + response.getTotalParcels());
            System.out.println("  Trucks Used: " + response.getTrucksUsed());
            System.out.println("  Overall Utilization: " + String.format("%.1f%%", response.getUtilizationPercentage()));
            
            System.out.println("\n  Truck Details (notice each truck appears only once):");
            for (TruckAssignment assignment : response.getTruckAssignments()) {
                System.out.println("    " + assignment.getTruckId() + ":");
                System.out.println("      Capacity: " + String.format("%.0f", assignment.getTruckCapacity()));
                System.out.println("      Used: " + String.format("%.0f", assignment.getTotalVolume()));
                System.out.println("      Remaining: " + String.format("%.0f", assignment.getRemainingCapacity()));
                System.out.println("      Utilization: " + String.format("%.1f%%", assignment.getUtilizationPercentage()));
                System.out.print("      Parcels: ");
                for (TruckAssignment.ParcelInfo p : assignment.getParcels()) {
                    System.out.print(p.getParcelId() + "(" + String.format("%.0f", p.getVolume()) + ") ");
                }
                System.out.println();
            }
        } else if (response.hasUnassignedParcels()) {
            System.out.println("⚠ PARTIAL SUCCESS");
        } else {
            System.out.println("✗ ERROR: " + response.getErrorMessage());
        }
        System.out.println();
    }

    private static void testInvalidInput() {
        System.out.println("Test 5: Error - Invalid Input");
        System.out.println("------------------------------");

        TruckAssingmentAlgoService service = new TruckAssingmentAlgoServiceAlgoImpl();

        List<Pair<String, Double>> trucks = Arrays.asList(
            new ImmutablePair<>("TRUCK-1", 100.0)
        );

        List<Pair<String, Double>> parcels = Arrays.asList(
            new ImmutablePair<>("INVALID", -10.0)
        );

        AssignmentResponse response = service.assignParcelsToTrucks(trucks, parcels);

        if (response.isSuccess()) {
            System.out.println("✗ UNEXPECTED SUCCESS");
        } else {
            System.out.println("✓ ERROR HANDLED");
            System.out.println("  Code: " + response.getErrorCode());
            System.out.println("  Message: " + response.getErrorMessage());
        }
        System.out.println();
    }
}
