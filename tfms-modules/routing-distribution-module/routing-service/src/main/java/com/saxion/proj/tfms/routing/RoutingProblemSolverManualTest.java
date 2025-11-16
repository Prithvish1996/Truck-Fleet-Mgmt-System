package com.saxion.proj.tfms.routing;

import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.routing.model.*;
import com.saxion.proj.tfms.routing.service.computation.helper.tomtom.RoutingProblemSolver;
import com.saxion.proj.tfms.routing.service.computation.helper.tomtom.TomTomRouteCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Manual test for TomTom RoutingProblemSolver
 * Tests the shift-based clustering and route optimization
 * 
 * Run this as a standalone main method - no Spring Boot needed!
 */
public class RoutingProblemSolverManualTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  TEST: 3 PARCELS SAME LOCATION, FAR DISTANCE (3+ HOURS)       ");
        System.out.println("==================================================================\n");

        try {
            // Run Test 1: Simple 3 parcels at same FAR location
            System.out.println("🧪 TEST 1: 3 Parcels at Same FAR Location (3+ hours)");
            System.out.println("─────────────────────────────────────────────────────────────\n");
            runSimpleTest();

            System.out.println("\n" + "=".repeat(66) + "\n");

            // Run Test 2: Same scenario as Test 1 (identical 3 parcels same FAR location)
            System.out.println("🧪 TEST 2: Scenario 2 - Same FAR Location Test (3+ hours)");
            System.out.println("─────────────────────────────────────────────────────────────\n");
            runComplexTest();

        } catch (Exception e) {
            System.err.println("\n❌ Error during testing:");
            e.printStackTrace();
        }
    }

    /**
     * Test 1: Simple test with 3 parcels at the same location from one warehouse
     */
    private static void runSimpleTest() throws Exception {
        // Create the solver with dependencies
        RoutingProblemSolver solver = createSolverWithDependencies();

        // Create simple test scenario
        RouteCoordinatesGroup testData = createSimpleTestScenario();

        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("SIMPLE TEST SCENARIO:");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("📍 Depot: Amsterdam Central (" +
            testData.getDepot().getLatitude() + ", " +
            testData.getDepot().getLongitude() + ")");
        System.out.println("🏢 Warehouse: Amsterdam North (" +
            testData.getWarehouse().getLatitude() + ", " +
            testData.getWarehouse().getLongitude() + ")");
        System.out.println("📦 Total Parcels: " + testData.getParcelList().size());
        System.out.println("📍 All parcels going to SAME location");
        System.out.println("\nParcels to deliver:");
        for (Parcel parcel : testData.getParcelList()) {
            System.out.printf("   • %s - %.1f units → %s (%.6f, %.6f)\n",
                parcel.getParcelName(),
                parcel.getVolume(),
                parcel.getRecipientName(),
                parcel.getDeliveryLatitude(),
                parcel.getDeliveryLongitude());
        }
        System.out.println("\n═══════════════════════════════════════════════════════════════\n");

        // Calculate route
        System.out.println("🚀 Starting simple route calculation...\n");
        long startTime = System.currentTimeMillis();

        List<Stop> stops = solver.calculateRoute(testData);

        long endTime = System.currentTimeMillis();
        System.out.println("✅ Simple route calculation completed in " + (endTime - startTime) + "ms\n");

        // Analyze results
        analyzeResults(stops, testData, solver, "SIMPLE");
    }

    /**
     * Test 2: Scenario 2 test with 3 parcels at same location (identical to Test 1 but different implementation)
     */
    private static void runComplexTest() throws Exception {
        // Create the solver with dependencies
        RoutingProblemSolver solver = createSolverWithDependencies();

        // Create complex test scenario
        RouteCoordinatesGroup testData = createComplexTestScenario();

        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("SCENARIO 2 TEST:");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("📍 Depot: Amsterdam Central (" +
            testData.getDepot().getLatitude() + ", " +
            testData.getDepot().getLongitude() + ")");
        System.out.println("🏢 Warehouse: Amsterdam North (" +
            testData.getWarehouse().getLatitude() + ", " +
            testData.getWarehouse().getLongitude() + ")");
        System.out.println("📦 Total Parcels: " + testData.getParcelList().size());
        System.out.println("📍 All parcels going to SAME location");
        System.out.println("\nParcels to deliver:");
        for (Parcel parcel : testData.getParcelList()) {
            System.out.printf("   • %s - %.1f units → %s (%.6f, %.6f)\n",
                parcel.getParcelName(),
                parcel.getVolume(),
                parcel.getRecipientName(),
                parcel.getDeliveryLatitude(),
                parcel.getDeliveryLongitude());
        }
        System.out.println("\n═══════════════════════════════════════════════════════════════\n");

        // Calculate routecreateComplexTestScenario
        System.out.println("🚀 Starting scenario 2 route calculation...\n");
        long startTime = System.currentTimeMillis();

        List<Stop> stops = solver.calculateRoute(testData);

        long endTime = System.currentTimeMillis();
        System.out.println("✅ Scenario 2 route calculation completed in " + (endTime - startTime) + "ms\n");

        // Analyze results
        analyzeResults(stops, testData, solver, "SCENARIO2");
    }

    /**
     * Analyze and display the results of route calculation
     */
    private static void analyzeResults(List<Stop> stops, RouteCoordinatesGroup testData, RoutingProblemSolver solver, String testType) {
        // Check parcel delivery status
        int deliveredCount = 0;
        for (Stop stop : stops) {
            if (stop.getStopType() == StopType.CUSTOMER) {
                deliveredCount += stop.getParcelsToDeliver().size();
            }
        }

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              " + testType + " TEST - PARCEL DELIVERY STATUS           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("📊 Total Parcels: " + testData.getParcelList().size());
        System.out.println("✅ Delivered: " + deliveredCount);
        System.out.println("❌ Not Delivered: " + (testData.getParcelList().size() - deliveredCount));

        if (deliveredCount < testData.getParcelList().size()) {
            System.out.println("\n⚠️  MISSING PARCELS:");
            java.util.Set<String> deliveredIds = new java.util.HashSet<>();
            for (Stop stop : stops) {
                if (stop.getParcelsToDeliver() != null) {
                    for (Parcel p : stop.getParcelsToDeliver()) {
                        deliveredIds.add(p.getParcelName());
                    }
                }
            }
            for (Parcel p : testData.getParcelList()) {
                if (!deliveredIds.contains(p.getParcelName())) {
                    System.out.printf("   • %s (%.6f, %.6f) → %s\n",
                        p.getParcelName(),
                        p.getDeliveryLatitude(),
                        p.getDeliveryLongitude(),
                        p.getRecipientName());
                }
            }
        }
        System.out.println();

        // Print results with metrics
        printResults(stops, solver, testType);
    }

    /**
     * Create RoutingProblemSolver with manual dependency injection
     */
    private static RoutingProblemSolver createSolverWithDependencies() throws Exception {
        // Create TomTom calculator
        TomTomRouteCalculator tomTomCalculator = new TomTomRouteCalculator();


        RoutingProblemSolver solver = new RoutingProblemSolver();
        java.lang.reflect.Field calculatorField = RoutingProblemSolver.class.getDeclaredField("tomTomRouteCalculator");
        calculatorField.setAccessible(true);
        calculatorField.set(solver, tomTomCalculator);

        return solver;
    }

    /**
     * Create simple test scenario with just 3 parcels at the same location from one warehouse
     */
    private static RouteCoordinatesGroup createSimpleTestScenario() {
        // Depot in Amsterdam Central Station
        Coordinates depot = new Coordinates(52.379189, 4.899431);

        // Warehouse in Amsterdam North
        Coordinates warehouse = new Coordinates(52.400000, 4.900000);

        List<Parcel> parcelList = new ArrayList<>();
        List<Coordinates> parcelCoordinates = new ArrayList<>();

        // Far delivery location for all 3 parcels (Rotterdam - 3+ hours away)
        Coordinates deliveryLocation = new Coordinates(81.922500, 4.479170);

        // Create 3 parcels going to the same location
        for (int i = 1; i <= 3; i++) {
            Parcel parcel = new Parcel();
            parcel.setParcelId((long) i);
            parcel.setParcelName("PARCEL-" + String.format("%03d", i));
            parcel.setVolume(10.0 + (i * 5)); // Different volumes: 15, 20, 25 units
            parcel.setWarehouseId(1L);
            parcel.setWarehouseLatitude(warehouse.getLatitude());
            parcel.setWarehouseLongitude(warehouse.getLongitude());
            parcel.setDeliveryLatitude(deliveryLocation.getLatitude());
            parcel.setDeliveryLongitude(deliveryLocation.getLongitude());
            parcel.setRecipientName("Rotterdam Harbor - Building " + i);
            parcel.setRecipientPhone("+31101234560" + i);
            parcel.setDeliveryInstructions("FAR LOCATION (3+ hours) - Harbor building " + i + " - Port security check");

            parcelList.add(parcel);
            parcelCoordinates.add(new Coordinates(parcel.getDeliveryLatitude(), parcel.getDeliveryLongitude()));
        }

        return RouteCoordinatesGroup.builder()
                .depot(depot)
                .warehouse(warehouse)
                .parcels(parcelCoordinates)
                .parcelList(parcelList)
                .build();
    }

    /**
     * Create scenario 2 test with only 3 parcels at the same location from one warehouse
     */
    private static RouteCoordinatesGroup createComplexTestScenario() {
        // Depot in Amsterdam Central Station
        Coordinates depot = new Coordinates(52.379189, 4.899431);

        // Warehouse in Amsterdam North
        Coordinates warehouse = new Coordinates(52.400000, 4.900000);

        List<Parcel> parcelList = new ArrayList<>();
        List<Coordinates> parcelCoordinates = new ArrayList<>();

        System.out.println("\n🎯 SCENARIO 2 TEST:");
        System.out.println("   • Only 3 parcels at SAME location (Rotterdam Harbor - 3+ hours away)");
        System.out.println("   • Testing LONG DISTANCE delivery efficiency\n");

        // ============================================================
        // ONLY 3 PARCELS AT SAME LOCATION (Rotterdam - 3+ hours away)
        // ============================================================
        Coordinates sameLocation = new Coordinates(81.922500, 4.479170); // Rotterdam harbor

        for (int i = 1; i <= 3; i++) {
            Parcel p = new Parcel();
            p.setParcelId((long) i);
            p.setParcelName("P" + String.format("%03d", i));
            p.setVolume(15.0 + (i * 5));
            p.setWarehouseId(1L);
            p.setWarehouseLatitude(warehouse.getLatitude());
            p.setWarehouseLongitude(warehouse.getLongitude());
            p.setDeliveryLatitude(sameLocation.getLatitude());
            p.setDeliveryLongitude(sameLocation.getLongitude());
            p.setRecipientName("Rotterdam Harbor - Warehouse " + i);
            p.setRecipientPhone("+31101234560" + i);
            p.setDeliveryInstructions("FAR LOCATION (3+ hours) - Port warehouse " + i + " - Security clearance needed");
            parcelList.add(p);
            parcelCoordinates.add(new Coordinates(p.getDeliveryLatitude(), p.getDeliveryLongitude()));
        }


        return RouteCoordinatesGroup.builder()
                .depot(depot)
                .warehouse(warehouse)
                .parcels(parcelCoordinates)
                .parcelList(parcelList)
                .build();
    }

    /**
     * Helper method to create a parcel from address data
     */
    private static Parcel createParcel(long id, String[] data, Coordinates warehouse, double volume) {
        Parcel p = new Parcel();
        p.setParcelId(id);
        p.setParcelName(data[0]);
        p.setVolume(volume);
        p.setWarehouseId(1L);
        p.setWarehouseLatitude(warehouse.getLatitude());
        p.setWarehouseLongitude(warehouse.getLongitude());
        p.setDeliveryLatitude(Double.parseDouble(data[2]));
        p.setDeliveryLongitude(Double.parseDouble(data[3]));
        p.setRecipientName(data[4]);
        p.setRecipientPhone("+31612" + String.format("%06d", id));
        p.setDeliveryInstructions(data[1] + " - " + data[5]);
        return p;
    }

    /**
     * Print the calculated route in a nice format
     */
    private static void printResults(List<Stop> stops, RoutingProblemSolver solver, String testType) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         " + testType + " TEST - ROUTE CALCULATION RESULTS         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Separate stops by type
        List<Stop> customerStops = stops.stream()
            .filter(s -> s.getStopType() == StopType.CUSTOMER)
            .collect(java.util.stream.Collectors.toList());

        // Count total parcels in all stops
        int totalParcelsInRoute = 0;
        for (Stop stop : customerStops) {
            if (stop.getParcelsToDeliver() != null) {
                totalParcelsInRoute += stop.getParcelsToDeliver().size();
            }
        }

        System.out.println("📊 SUMMARY:");
        System.out.println("   📦 Total Parcels Delivered: " + totalParcelsInRoute);
        System.out.println("   🎯 Total Customer Stops (optimized): " + customerStops.size());
        System.out.println("   🚚 Complete Route Stops: " + stops.size() + " (Depot + Warehouse + " + customerStops.size() + " customers)");
        System.out.println("   📍 Fixed Stops: 2 (Depot=0, Warehouse=1)");
        System.out.println("   🔄 Optimized Stops: " + customerStops.size() + " (Customer deliveries)");


        
        System.out.println();

        System.out.println("🚚 COMPLETE ROUTE SEQUENCE:");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("ℹ️  Stop 0 = DEPOT (fixed), Stop 1 = WAREHOUSE (fixed), Stops 2+ = CUSTOMERS (optimized)");
        System.out.println("═══════════════════════════════════════════════════════════════");

        // Display ALL stops in order
        for (int i = 0; i < stops.size(); i++) {
            Stop stop = stops.get(i);
            String icon = getStopIcon(stop.getStopType().toString());
            String fixedStatus = "";

            if (i == 0) {
                fixedStatus = " (FIXED - Always first)";
            } else if (i == 1) {
                fixedStatus = " (FIXED - Always second)";
            } else {
                fixedStatus = " (OPTIMIZED)";
            }

            System.out.println("\n[Stop " + i + "] " + icon + " " + stop.getStopType() + fixedStatus);
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.printf("  📍 Location: %.6f, %.6f\n", 
                stop.getCoordinates().getLatitude(), 
                stop.getCoordinates().getLongitude());
            
            if (stop.getParcelsToDeliver() != null && !stop.getParcelsToDeliver().isEmpty()) {
                System.out.println("  📦 Parcels (" + stop.getParcelsToDeliver().size() + "):");
                for (Parcel parcel : stop.getParcelsToDeliver()) {
                    System.out.printf("     • %s - %.1f units → %s\n",
                        parcel.getParcelName(),
                        parcel.getVolume(),
                        parcel.getRecipientName());
                    if (parcel.getDeliveryInstructions() != null && !parcel.getDeliveryInstructions().isEmpty()) {
                        System.out.println("       Note: " + parcel.getDeliveryInstructions());
                    }
                }
            } else if (stop.getStopType() == StopType.DEPOT) {
                System.out.println("  🏁 Starting point - Load truck and begin route");
            } else if (stop.getStopType() == StopType.WAREHOUSE) {
                System.out.println("  📦 Pick up parcels from warehouse");
            }
        }

        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("\n✅ Route Calculation Complete!\n");
    }

    private static String getStopIcon(String stopType) {
        switch (stopType.toUpperCase()) {
            case "DEPOT":
                return "🏁";
            case "WAREHOUSE":
                return "🏢";
            case "CUSTOMER":
                return "🏠";
            default:
                return "📍";
        }
    }

    /**
     * Format seconds into human-readable time string
     */
    private static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
