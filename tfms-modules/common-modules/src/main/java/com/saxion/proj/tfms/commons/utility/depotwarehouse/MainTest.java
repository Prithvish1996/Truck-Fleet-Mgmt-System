package com.saxion.proj.tfms.commons.utility.depotwarehouse;

import com.saxion.proj.tfms.commons.utility.depotwarehouse.helper.GraphHopperUtil;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.TruckWarehouseAssignment;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.Depot;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.Truck;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.Warehouse;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.service.AssignmentService;

import java.util.Arrays;
import java.util.List;

/**
 * MainTest - Truck-warehouse assignment using GraphHopper API
 *
 * GraphHopper:
 * - No map files needed!
 * - Uses hosted GraphHopper API (cloud-based)
 * - Requires free API key from https://www.graphhopper.com/
 * - Free tier: 500 requests/day
 */
public class MainTest {
    
    // TODO: Replace with your actual GraphHopper API key
    private static final String GRAPHHOPPER_API_KEY = "b66d69eb-b2eb-41a8-835b-07770a92aaa4";

    public static void main(String[] args) {
        System.out.println("=== Truck-Warehouse Assignment Test (GraphHopper) ===\n");

        try {
            // Validate API key is set
            if (GRAPHHOPPER_API_KEY.equals("YOUR_API_KEY_HERE")) {
                System.err.println("❌ ERROR: Please set your GraphHopper API key!");
                System.err.println("\n1. Get free API key from: https://www.graphhopper.com/");
                System.err.println("2. Update GRAPHHOPPER_API_KEY in MainTest.java");
                System.err.println("3. Run again\n");
                System.exit(1);
            }

            System.out.println("Initializing GraphHopper routing...");

            // Create GraphHopper utility with your API key
            // Profile options: "car", "truck", "bike", "foot", "motorcycle"
            GraphHopperUtil ghUtil = GraphHopperUtil.withHostedAPI(GRAPHHOPPER_API_KEY, "car");
            AssignmentService service = new AssignmentService(ghUtil);

            System.out.println("✓ GraphHopper ready (using hosted API)\n");

            System.out.println("Creating test data (Monaco coordinates)...");
            Depot depot1 = new Depot("D1", "Depot A", 43.7384, 7.4246);
            depot1.trucks.add(new Truck("Truck-1"));
            depot1.trucks.add(new Truck("Truck-2"));

            Warehouse wh1 = new Warehouse("W1", "Warehouse X", 43.7350, 7.4200);
            Warehouse wh2 = new Warehouse("W2", "Warehouse Y", 43.7400, 7.4300);

            List<Depot> depots = List.of(depot1);
            List<Warehouse> warehouses = Arrays.asList(wh1, wh2);

            // Test individual route first
            System.out.println("Testing single route...");
            double testDist = ghUtil.computeDistance(depot1.lat, depot1.lon, wh1.lat, wh1.lon);
            double testTime = ghUtil.computeTime(depot1.lat, depot1.lon, wh1.lat, wh1.lon);
            System.out.printf("  Depot A -> Warehouse X: %.2f km, %.2f min\n\n", testDist, testTime);

            System.out.println("Computing assignments with real routing...\n");
            List<TruckWarehouseAssignment> results = service.assignTrucks(depots, warehouses);
            
            System.out.println("\n=== Results ===");
            if (results.isEmpty()) {
                System.out.println("No assignments made (distances/times exceeded thresholds)");
            } else {
                results.forEach(System.out::println);
            }
            System.out.println("\n✓ Test completed successfully!");
            System.out.println("Note: Using GraphHopper hosted API (500 requests/day free tier)");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Check your API key is valid");
            System.err.println("2. Verify internet connection");
            System.err.println("3. Check if you've exceeded rate limit (500/day)");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
