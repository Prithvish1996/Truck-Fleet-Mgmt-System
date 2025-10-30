package com.saxion.proj.tfms.routing;

import com.saxion.proj.tfms.routing.dto.*;
import com.saxion.proj.tfms.routing.service.DistanceMatrixService;
import com.saxion.proj.tfms.routing.service.OrToolsVrpService;
import com.saxion.proj.tfms.routing.service.VRPProvider;
import com.saxion.proj.tfms.routing.vrp.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Manual test for OR-Tools VRP Service
 * Run this as a main method to test the VRP optimization using Google OR-Tools
 *
 * This uses FREE OSRM API for distance calculation - no API key needed!
 */
public class OrToolsVrpServiceManualTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("      OR-TOOLS VRP SERVICE TEST (FREE - No API Key!)             ");
        System.out.println("==================================================================\n");

        try {
            // Create all service components manually (since we're not using Spring)
            VRPProvider vrpService = createVrpServiceWithDependencies();

            // Create test request
            VrpRequestDto request = createLargeTestScenario();

            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("TEST SCENARIO:");
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("📍 Depot: Berlin City Center (52.520008, 13.404954)");
            System.out.println("🚚 Trucks: " + request.getTrucks().size());
            for (TruckInfo truck : request.getTrucks()) {
                System.out.printf("   • %s (capacity: %.0f cubic units)\n",
                        truck.getTruckName(), truck.getVolume());
            }
            System.out.println("\n📦 Parcels: " + request.getParcels().size());

            // Group by warehouse
            java.util.Map<String, java.util.List<ParcelInfo>> parcelsByWarehouse = new java.util.HashMap<>();
            for (ParcelInfo parcel : request.getParcels()) {
                parcelsByWarehouse.computeIfAbsent(parcel.getWarehouseId(), k -> new ArrayList<>()).add(parcel);
            }

            for (java.util.Map.Entry<String, java.util.List<ParcelInfo>> entry : parcelsByWarehouse.entrySet()) {
                System.out.printf("\n   From %s (%d parcels):\n", entry.getKey(), entry.getValue().size());
                for (ParcelInfo parcel : entry.getValue()) {
                    System.out.printf("      • %s - %s (%.0f units) → %s\n",
                            parcel.getParcelId(),
                            parcel.getParcelName(),
                            parcel.getVolume(),
                            parcel.getRecipientName());
                }
            }

            System.out.println("\n═══════════════════════════════════════════════════════════════\n");

            // Call the service
            VrpResponseDto response = vrpService.optimizeRoutes(request);

            // Print results
            printResults(response);

        } catch (Exception e) {
            System.err.println("\n❌ Error during VRP optimization:");
            e.printStackTrace();
        }
    }

    /**
     * Create VRP Service with all dependencies manually wired
     * (Since we're not using Spring's dependency injection)
     */
    private static OrToolsVrpService createVrpServiceWithDependencies() throws Exception {
        // Create all the dependencies
        com.saxion.proj.tfms.routing.vrp.VrpRequestValidator validator = 
            new com.saxion.proj.tfms.routing.vrp.VrpRequestValidator();
        
        com.saxion.proj.tfms.routing.vrp.CapacityCalculator capacityCalculator = 
            new com.saxion.proj.tfms.routing.vrp.CapacityCalculator();
        
        com.saxion.proj.tfms.routing.vrp.LocationMapper locationMapper = 
            new com.saxion.proj.tfms.routing.vrp.LocationMapper();
        
        com.saxion.proj.tfms.routing.vrp.WarehouseGroupingStrategy warehouseStrategy = 
            new com.saxion.proj.tfms.routing.vrp.WarehouseGroupingStrategy();
        
        DistanceMatrixService distanceMatrixService = new DistanceMatrixService();
        
        // Create builder with its dependency
        com.saxion.proj.tfms.routing.vrp.CostMatrixBuilder costMatrixBuilder = 
            new com.saxion.proj.tfms.routing.vrp.CostMatrixBuilder();
        java.lang.reflect.Field capacityCalcField = 
            com.saxion.proj.tfms.routing.vrp.CostMatrixBuilder.class.getDeclaredField("capacityCalculator");
        capacityCalcField.setAccessible(true);
        capacityCalcField.set(costMatrixBuilder, capacityCalculator);
        
        // Create constraint handler
        com.saxion.proj.tfms.routing.vrp.ConstraintHandler constraintHandler = 
            new com.saxion.proj.tfms.routing.vrp.ConstraintHandler();
        
        // Create solution parser with its dependency
        com.saxion.proj.tfms.routing.vrp.SolutionParser solutionParser = 
            new com.saxion.proj.tfms.routing.vrp.SolutionParser();
        java.lang.reflect.Field locationMapperField = 
            com.saxion.proj.tfms.routing.vrp.SolutionParser.class.getDeclaredField("locationMapper");
        locationMapperField.setAccessible(true);
        locationMapperField.set(solutionParser, locationMapper);
        
        // Create OR-Tools solver with its dependencies
        com.saxion.proj.tfms.routing.vrp.OrToolsSolver orToolsSolver = 
            new com.saxion.proj.tfms.routing.vrp.OrToolsSolver();
        
        java.lang.reflect.Field costMatrixField = 
            com.saxion.proj.tfms.routing.vrp.OrToolsSolver.class.getDeclaredField("costMatrixBuilder");
        costMatrixField.setAccessible(true);
        costMatrixField.set(orToolsSolver, costMatrixBuilder);
        
        java.lang.reflect.Field constraintField = 
            com.saxion.proj.tfms.routing.vrp.OrToolsSolver.class.getDeclaredField("constraintHandler");
        constraintField.setAccessible(true);
        constraintField.set(orToolsSolver, constraintHandler);
        
        java.lang.reflect.Field parserField = 
            com.saxion.proj.tfms.routing.vrp.OrToolsSolver.class.getDeclaredField("solutionParser");
        parserField.setAccessible(true);
        parserField.set(orToolsSolver, solutionParser);
        
        // Create main VRP service and inject all dependencies
        OrToolsVrpService vrpService = new OrToolsVrpService();
        
        java.lang.reflect.Field validatorField = 
            OrToolsVrpService.class.getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(vrpService, validator);
        
        java.lang.reflect.Field warehouseStrategyField = 
            OrToolsVrpService.class.getDeclaredField("warehouseStrategy");
        warehouseStrategyField.setAccessible(true);
        warehouseStrategyField.set(vrpService, warehouseStrategy);
        
        java.lang.reflect.Field capacityCalculatorField = 
            OrToolsVrpService.class.getDeclaredField("capacityCalculator");
        capacityCalculatorField.setAccessible(true);
        capacityCalculatorField.set(vrpService, capacityCalculator);
        
        java.lang.reflect.Field locationMapperServiceField = 
            OrToolsVrpService.class.getDeclaredField("locationMapper");
        locationMapperServiceField.setAccessible(true);
        locationMapperServiceField.set(vrpService, locationMapper);
        
        java.lang.reflect.Field distanceMatrixServiceField = 
            OrToolsVrpService.class.getDeclaredField("distanceMatrixService");
        distanceMatrixServiceField.setAccessible(true);
        distanceMatrixServiceField.set(vrpService, distanceMatrixService);
        
        java.lang.reflect.Field solverField = 
            OrToolsVrpService.class.getDeclaredField("orToolsSolver");
        solverField.setAccessible(true);
        solverField.set(vrpService, orToolsSolver);
        
        return vrpService;
    }

    /**
     * Create a realistic test scenario with multiple trucks and warehouses
     * - 6 trucks with different capacities
     * - 12 parcels from 3 different warehouses
     */
    private static VrpRequestDto createLargeTestScenario() {
        VrpRequestDto request = new VrpRequestDto();

        // Depot in Berlin City Center
        request.setDepot(new DepotInfo(52.520008, 13.404954));

        // Set VRP metric - now with CVRP capacity-aware options!
        // Try these different options:
        // request.setMetric(VrpMetric.DISTANCE);                 // Classic distance-only
        // request.setMetric(VrpMetric.TIME);                     // Classic time-only
        // request.setMetric(VrpMetric.BOTH);                     // Distance + time
        // request.setMetric(VrpMetric.DISTANCE_CAPACITY);        // Distance + capacity efficiency
        // request.setMetric(VrpMetric.TIME_CAPACITY);            // Time + capacity efficiency
        request.setMetric(VrpMetric.DISTANCE_CAPACITY);      // Triple optimization (RECOMMENDED)

        // 3 Trucks with different capacities
        List<TruckInfo> trucks = new ArrayList<>();
        trucks.add(new TruckInfo("TRUCK-001", 150.0));
        trucks.add(new TruckInfo("TRUCK-002", 120.0));

        request.setTrucks(trucks);

        // 10 Parcels from 3 warehouses
        List<ParcelInfo> parcels = new ArrayList<>();

        // Warehouse A (North Berlin) - 4 parcels
        parcels.add(new ParcelInfo(
                "P001", "Electronics Package", 30.0, "WAREHOUSE-A",
                52.550008, 13.414954,  // Warehouse A (North)
                52.525008, 13.424954,  // Customer 1
                "John Doe"
        ));

        parcels.add(new ParcelInfo(
                "P002", "Furniture Package", 45.0, "WAREHOUSE-A",
                52.550008, 13.414954,  // Warehouse A
                52.535008, 13.434954,  // Customer 2
                "Jane Smith"
        ));

        parcels.add(new ParcelInfo(
                "P003", "Books Package", 20.0, "WAREHOUSE-A",
                52.550008, 13.414954,  // Warehouse A
                52.540008, 13.444954,  // Customer 3
                "Alice Johnson"
        ));

        parcels.add(new ParcelInfo(
                "P004", "Office Supplies", 25.0, "WAREHOUSE-A",
                52.550008, 13.414954,  // Warehouse A
                52.545008, 13.404954,  // Customer 4
                "Bob Wilson"
        ));

        // Warehouse B (West Berlin) - 3 parcels
        parcels.add(new ParcelInfo(
                "P005", "Clothing Package", 35.0, "WAREHOUSE-B",
                52.510008, 13.364954,  // Warehouse B (West)
                52.515008, 13.374954,  // Customer 5
                "Charlie Brown"
        ));

        parcels.add(new ParcelInfo(
                "P0071", "Sports Equipment", 40.0, "WAREHOUSE-B",
                52.510008, 13.364954,  // Warehouse B
                52.505008, 13.354954,  // Customer 6
                "Diana Prince"
        ));

        parcels.add(new ParcelInfo(
                "P007", "Kitchen Items", 30.0, "WAREHOUSE-B",
                52.510008, 13.364954,  // Warehouse B
                52.500008, 13.344954,  // Customer 7
                "Eve Adams"
        ));

        // Warehouse C (East Berlin) - 3 parcels
        parcels.add(new ParcelInfo(
                "P008", "Garden Tools", 35.0, "WAREHOUSE-C",
                52.520008, 13.454954,  // Warehouse C (East)
                52.522008, 13.464954,  // Customer 8
                "Frank Miller"
        ));

        parcels.add(new ParcelInfo(
                "P009", "Toys Package", 25.0, "WAREHOUSE-C",
                52.520008, 13.454954,  // Warehouse C
                52.518008, 13.474954,  // Customer 9
                "Grace Lee"
        ));

        parcels.add(new ParcelInfo(
                "P010", "Pet Supplies", 30.0, "WAREHOUSE-C",
                52.520008, 13.454954,  // Warehouse C
                52.515008, 13.484954,  // Customer 10
                "Henry Taylor"
        ));
        parcels.add(new ParcelInfo(
                "P011", "Pet Supplies", 130.0, "WAREHOUSE-C",
                52.520008, 13.454954,  // Warehouse C
                52.515008, 13.484954,  // Customer 10
                "Henry Taylor"
        ));
        parcels.add(new ParcelInfo(
                "P013", "Pet Supplies", 30.0, "WAREHOUSE-C",
                52.520008, 13.454954,  // Warehouse C
                52.515008, 13.484954,  // Customer 10
                "Henry Taylor"
        ));

        request.setParcels(parcels);

        return request;
    }

    /**
     * Print optimization results in a nice format
     */
    private static void printResults(VrpResponseDto response) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              VRP OPTIMIZATION RESULTS                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("📊 SUMMARY:");
        System.out.println("   Trucks Used: " + response.getTotalVehiclesUsed() + " (out of 3 available)");
        System.out.println("   Total Distance: " + String.format("%.2f km", response.getTotalDistance() / 1000.0));
        System.out.println("   Total Time: " + formatTime(response.getTotalTime()));
        System.out.println();

        System.out.println("🚚 TRUCK ROUTES:");
        System.out.println("═══════════════════════════════════════════════════════════════");

        for (int i = 0; i < response.getTruckRoutes().size(); i++) {
           TruckRoute route = response.getTruckRoutes().get(i);

            System.out.println("\n[Route " + (i + 1) + "] " + route.getTruckId());
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println("  Warehouse: " + route.getWarehouseVisited());
            System.out.println("  Parcels: " + route.getAssignedParcels().size() + " parcels");
            System.out.println("  Distance: " + String.format("%.2f km", route.getDistance() / 1000.0));
            System.out.println("  Time: " + formatTime(route.getTransportTime()));

            // Extract delivery sequence
            List<String> deliverySequence = new ArrayList<>();
            for (Activity activity : route.getActivities()) {
                if ("deliverShipment".equals(activity.getType())) {
                    deliverySequence.add(activity.getId());
                }
            }
            if (!deliverySequence.isEmpty()) {
                System.out.println("  🎯 Delivery Order: " + String.join(" → ", deliverySequence));
            }

            System.out.println("\n  📍 DETAILED STOPS:");

            int stopNumber = 1;
            int deliveryNumber = 1;
            for (Activity activity : route.getActivities()) {
                String icon = getActivityIcon(activity.getType());
                String description = formatActivity(activity);

                // Add delivery order number for deliver activities
                if ("deliverShipment".equals(activity.getType())) {
                    System.out.printf("     %s Stop %d: %s ← DELIVERY #%d\n",
                            icon, stopNumber++, description, deliveryNumber++);
                } else {
                    System.out.printf("     %s Stop %d: %s\n", icon, stopNumber++, description);
                }

                System.out.printf("        📍 Location: %.6f, %.6f\n",
                        activity.getLatitude(), activity.getLongitude());

                if (activity.getArrivalTime() > 0) {
                    System.out.printf("        ⏰ Arrival: %s (drove %s)\n",
                            formatTime(activity.getArrivalTime()),
                            formatTime(activity.getDrivingTime()));
                }
                System.out.println();
            }
        }

        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("\n✅ Optimization Complete!\n");
    }

    private static String getActivityIcon(String type) {
        switch (type) {
            case "start": return "🏁";
            case "pickupShipment": return "📦";
            case "deliverShipment": return "🎁";
            case "end": return "🏁";
            default: return "•";
        }
    }

    private static String formatActivity(Activity activity) {
        switch (activity.getType()) {
            case "start":
                return "START at depot";
            case "pickupShipment":
                return "PICKUP parcel " + activity.getId() + " from warehouse";
            case "deliverShipment":
                return "DELIVER parcel " + activity.getId() + " to customer";
            case "end":
                return "RETURN to depot";
            default:
                return activity.getType();
        }
    }

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
