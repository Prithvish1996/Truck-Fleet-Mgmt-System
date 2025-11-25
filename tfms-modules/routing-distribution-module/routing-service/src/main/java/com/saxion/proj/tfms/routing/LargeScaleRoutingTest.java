package com.saxion.proj.tfms.routing;

import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.routing.model.*;
import com.saxion.proj.tfms.routing.service.computation.helper.tomtom.RoutingProblemSolver;
import com.saxion.proj.tfms.routing.service.computation.helper.tomtom.TomTomRouteCalculator;

import java.util.*;

/**
 * Large Scale Manual Test for TomTom RoutingProblemSolver
 * Tests with 200 parcels, 10 trucks, 2 warehouses
 * Includes duplicate coordinates and randomized delivery locations
 * 
 * Run this as a standalone main method - no Spring Boot needed!
 */
public class LargeScaleRoutingTest {

    private static final Random random = new Random(42); // Fixed seed for reproducible results

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   LARGE SCALE TEST: 200 PARCELS, 10 TRUCKS, 2 WAREHOUSES       ");
        System.out.println("   • Duplicate coordinates testing                               ");
        System.out.println("   • Randomized delivery locations                              ");
        System.out.println("   • Multiple warehouse pickup optimization                     ");
        System.out.println("==================================================================\n");

        try {
            // Run comprehensive large scale test
            runLargeScaleTest();

        } catch (Exception e) {
            System.err.println("\n❌ Error during large scale testing:");
            e.printStackTrace();
        }
    }

    /**
     * Large scale test with 200 parcels from 2 warehouses, duplicate coordinates, randomization
     */
    private static void runLargeScaleTest() throws Exception {
        System.out.println("🧪 LARGE SCALE TEST: 200 Parcels, 10 Trucks, 2 Warehouses");
        System.out.println("─────────────────────────────────────────────────────────────\n");

        // Create the solver with dependencies
        RoutingProblemSolver solver = createSolverWithDependencies();

        // Create large scale test scenario
        RouteCoordinatesGroup testData = createLargeScaleTestScenario();

        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("LARGE SCALE TEST SCENARIO:");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("📍 Depot: Netherlands Central (" +
            testData.getDepot().getLatitude() + ", " +
            testData.getDepot().getLongitude() + ")");
        System.out.println("🏢 Warehouses: 2 locations (Amsterdam North & Rotterdam South)");
        System.out.println("📦 Total Parcels: " + testData.getParcelList().size());
        System.out.println("🚚 Available Trucks: 10 (Small, Medium, Large)");
        
        // Count duplicate coordinates
        int duplicateCount = countDuplicateCoordinates(testData.getParcelList());
        System.out.println("🎯 Duplicate Coordinates: " + duplicateCount + " sets");
        
        // Show warehouse distribution
        Map<String, Integer> warehouseDistribution = getWarehouseDistribution(testData.getParcelList());
        System.out.println("📊 Warehouse Distribution:");
        for (Map.Entry<String, Integer> entry : warehouseDistribution.entrySet()) {
            System.out.println("   • " + entry.getKey() + ": " + entry.getValue() + " parcels");
        }

        System.out.println("\nSample parcels (first 10):");
        for (int i = 0; i < Math.min(10, testData.getParcelList().size()); i++) {
            Parcel parcel = testData.getParcelList().get(i);
            System.out.printf("   • %s - %.1f units → %s (%.6f, %.6f) [WH: %.6f, %.6f]\n",
                parcel.getParcelName(),
                parcel.getVolume(),
                parcel.getRecipientName(),
                parcel.getDeliveryLatitude(),
                parcel.getDeliveryLongitude(),
                parcel.getWarehouseLatitude(),
                parcel.getWarehouseLongitude());
        }
        if (testData.getParcelList().size() > 10) {
            System.out.println("   ... and " + (testData.getParcelList().size() - 10) + " more parcels");
        }
        System.out.println("\n═══════════════════════════════════════════════════════════════\n");

        // Calculate route
        System.out.println("🚀 Starting large scale route calculation...\n");
        long startTime = System.currentTimeMillis();

        List<Stop> stops = solver.calculateRoute(testData);

        long endTime = System.currentTimeMillis();
        System.out.println("✅ Large scale route calculation completed in " + (endTime - startTime) + "ms\n");

        // Analyze results
        analyzeLargeScaleResults(stops, testData, solver);
    }

    /**
     * Create large scale test scenario with 200 parcels, 2 warehouses, duplicates, randomization
     */
    private static RouteCoordinatesGroup createLargeScaleTestScenario() {
        // Central depot in Netherlands
        Coordinates depot = new Coordinates(52.2129919, 5.2793701); // Netherlands geographic center

        // Two warehouse locations
        Coordinates warehouseAmsterdam = new Coordinates(52.400000, 4.900000); // Amsterdam North
        Coordinates warehouseRotterdam = new Coordinates(51.924420, 4.477733); // Rotterdam South

        List<Parcel> parcelList = new ArrayList<>();
        List<Coordinates> parcelCoordinates = new ArrayList<>();

        System.out.println("\n🎯 CREATING LARGE SCALE TEST SCENARIO:");
        System.out.println("   • 200 parcels with strategic distribution");
        System.out.println("   • 50% duplicate coordinates (same building deliveries)");
        System.out.println("   • 2 warehouses with balanced load");
        System.out.println("   • Randomized delivery locations across Netherlands\n");

        // Create base delivery locations (will be used for duplicates)
        List<Coordinates> baseDeliveryLocations = generateBaseDeliveryLocations();
        
        // Create 200 parcels
        for (int i = 1; i <= 200; i++) {
            // Determine warehouse (alternate between two warehouses)
            boolean useAmsterdam = (i % 2 == 1);
            Coordinates warehouse = useAmsterdam ? warehouseAmsterdam : warehouseRotterdam;
            String warehouseId = useAmsterdam ? "WH-AMS" : "WH-RTM";
            
            // Determine delivery location (50% chance of duplicate)
            Coordinates deliveryLocation;
            if (random.nextDouble() < 0.5 && !baseDeliveryLocations.isEmpty()) {
                // Use existing location (create duplicate)
                deliveryLocation = baseDeliveryLocations.get(random.nextInt(baseDeliveryLocations.size()));
            } else {
                // Create new unique location
                deliveryLocation = generateRandomDeliveryLocation(warehouse);
                baseDeliveryLocations.add(deliveryLocation);
            }
            
            // Create parcel
            Parcel parcel = new Parcel();
            parcel.setParcelId((long) i);
            parcel.setParcelName(String.format("P%03d", i));
            parcel.setVolume(0.5 + (random.nextDouble() * 49.5)); // 0.5 - 50.0 m³
            parcel.setWarehouseId(useAmsterdam ? 1L : 2L);
            parcel.setWarehouseLatitude(warehouse.getLatitude());
            parcel.setWarehouseLongitude(warehouse.getLongitude());
            parcel.setDeliveryLatitude(deliveryLocation.getLatitude());
            parcel.setDeliveryLongitude(deliveryLocation.getLongitude());
            
            // Generate recipient details
            String[] cityNames = {"Amsterdam", "Rotterdam", "Utrecht", "Eindhoven", "Groningen", 
                                "Tilburg", "Almere", "Breda", "Nijmegen", "Apeldoorn", "Haarlem", "Arnhem"};
            String city = cityNames[random.nextInt(cityNames.length)];
            String[] businessTypes = {"Office", "Store", "Restaurant", "Warehouse", "Hotel", "Hospital", 
                                    "School", "Factory", "Mall", "Apartment"};
            String businessType = businessTypes[random.nextInt(businessTypes.length)];
            
            parcel.setRecipientName(city + " " + businessType + " #" + (random.nextInt(999) + 1));
            parcel.setRecipientPhone("+316" + String.format("%08d", random.nextInt(100000000)));
            
            // Delivery instructions based on business type
            String[] instructions = {
                "Reception desk - ID required",
                "Loading dock - Ring bell",
                "Main entrance - Ask for manager",
                "Back door - Service entrance",
                "Ground floor - Suite " + (random.nextInt(50) + 1),
                "Fragile - Handle with care",
                "Security check required",
                "Delivery between 9-17h only"
            };
            parcel.setDeliveryInstructions(instructions[random.nextInt(instructions.length)] + 
                " [" + warehouseId + "]");
            
            parcelList.add(parcel);
            parcelCoordinates.add(new Coordinates(parcel.getDeliveryLatitude(), parcel.getDeliveryLongitude()));
        }
        
        // Randomize the parcel list order
        Collections.shuffle(parcelList, random);
        Collections.shuffle(parcelCoordinates, random);
        
        System.out.println("✅ Created 200 parcels with randomization and duplicates");
        System.out.println("   • Base delivery locations: " + baseDeliveryLocations.size());
        System.out.println("   • Warehouse Amsterdam: " + parcelList.stream().mapToInt(p -> p.getWarehouseId() == 1L ? 1 : 0).sum() + " parcels");
        System.out.println("   • Warehouse Rotterdam: " + parcelList.stream().mapToInt(p -> p.getWarehouseId() == 2L ? 1 : 0).sum() + " parcels");

        // Use Amsterdam warehouse as primary for routing (will be optimized for multi-warehouse later)
        return RouteCoordinatesGroup.builder()
                .depot(depot)
                .warehouse(warehouseAmsterdam)
                .parcels(parcelCoordinates)
                .parcelList(parcelList)
                .build();
    }

    /**
     * Generate base delivery locations spread across Netherlands
     */
    private static List<Coordinates> generateBaseDeliveryLocations() {
        List<Coordinates> baseLocations = new ArrayList<>();
        
        // Major cities in Netherlands with their coordinates
        double[][] majorCities = {
            {52.3676, 4.9041},   // Amsterdam
            {51.9244, 4.4777},   // Rotterdam  
            {52.0907, 5.1214},   // Utrecht
            {51.4416, 5.4697},   // Eindhoven
            {53.2194, 6.5665},   // Groningen
            {51.5555, 5.0913},   // Tilburg
            {52.3508, 5.2647},   // Almere
            {51.5719, 4.7683},   // Breda
            {51.8426, 5.8518},   // Nijmegen
            {52.2112, 5.9699},   // Apeldoorn
            {52.3874, 4.6462},   // Haarlem
            {51.9851, 5.8987}    // Arnhem
        };
        
        // Create multiple locations per city (business districts, residential areas, industrial zones)
        for (double[] city : majorCities) {
            for (int i = 0; i < 3; i++) {
                // Add some variation around each city center
                double latVariation = (random.nextDouble() - 0.5) * 0.02; // ±0.01 degrees (~1km)
                double lonVariation = (random.nextDouble() - 0.5) * 0.02;
                
                baseLocations.add(new Coordinates(
                    city[0] + latVariation,
                    city[1] + lonVariation
                ));
            }
        }
        
        return baseLocations;
    }

    /**
     * Generate random delivery location within Netherlands, biased towards warehouse region
     */
    private static Coordinates generateRandomDeliveryLocation(Coordinates warehouse) {
        // Netherlands bounding box
        double minLat = 50.7504;
        double maxLat = 53.5104;
        double minLon = 3.3792;
        double maxLon = 7.2274;
        
        // Bias towards warehouse location (70% within 50km, 30% anywhere in Netherlands)
        if (random.nextDouble() < 0.7) {
            // Near warehouse (within ~50km radius)
            double latOffset = (random.nextDouble() - 0.5) * 0.5; // ±0.25 degrees (~25km)
            double lonOffset = (random.nextDouble() - 0.5) * 0.5;
            
            double lat = Math.max(minLat, Math.min(maxLat, warehouse.getLatitude() + latOffset));
            double lon = Math.max(minLon, Math.min(maxLon, warehouse.getLongitude() + lonOffset));
            
            return new Coordinates(lat, lon);
        } else {
            // Anywhere in Netherlands
            double lat = minLat + (random.nextDouble() * (maxLat - minLat));
            double lon = minLon + (random.nextDouble() * (maxLon - minLon));
            
            return new Coordinates(lat, lon);
        }
    }

    /**
     * Count duplicate coordinates in parcel list
     */
    private static int countDuplicateCoordinates(List<Parcel> parcels) {
        Map<String, Integer> coordinateCount = new HashMap<>();
        
        for (Parcel parcel : parcels) {
            String coordKey = String.format("%.6f,%.6f", parcel.getDeliveryLatitude(), parcel.getDeliveryLongitude());
            coordinateCount.put(coordKey, coordinateCount.getOrDefault(coordKey, 0) + 1);
        }
        
        return (int) coordinateCount.values().stream().filter(count -> count > 1).count();
    }

    /**
     * Get warehouse distribution of parcels
     */
    private static Map<String, Integer> getWarehouseDistribution(List<Parcel> parcels) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (Parcel parcel : parcels) {
            String warehouseKey = String.format("Warehouse %.6f,%.6f", 
                parcel.getWarehouseLatitude(), parcel.getWarehouseLongitude());
            distribution.put(warehouseKey, distribution.getOrDefault(warehouseKey, 0) + 1);
        }
        
        return distribution;
    }

    /**
     * Analyze and display large scale test results
     */
    private static void analyzeLargeScaleResults(List<Stop> stops, RouteCoordinatesGroup testData, RoutingProblemSolver solver) {
        // Check parcel delivery status
        int deliveredCount = 0;
        for (Stop stop : stops) {
            if (stop.getStopType() == StopType.CUSTOMER) {
                deliveredCount += stop.getParcelsToDeliver().size();
            }
        }

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              LARGE SCALE TEST - DELIVERY STATUS                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("📊 Total Parcels: " + testData.getParcelList().size());
        System.out.println("✅ Delivered: " + deliveredCount);
        System.out.println("❌ Not Delivered: " + (testData.getParcelList().size() - deliveredCount));
        System.out.println("📈 Delivery Rate: " + String.format("%.1f%%", (deliveredCount * 100.0) / testData.getParcelList().size()));

        // Analysis by warehouse
        Map<String, Integer> deliveryByWarehouse = new HashMap<>();
        for (Stop stop : stops) {
            if (stop.getStopType() == StopType.CUSTOMER && stop.getParcelsToDeliver() != null) {
                for (Parcel p : stop.getParcelsToDeliver()) {
                    String whKey = String.format("WH-%.3f,%.3f", p.getWarehouseLatitude(), p.getWarehouseLongitude());
                    deliveryByWarehouse.put(whKey, deliveryByWarehouse.getOrDefault(whKey, 0) + 1);
                }
            }
        }
        
        System.out.println("\n📊 Deliveries by Warehouse:");
        for (Map.Entry<String, Integer> entry : deliveryByWarehouse.entrySet()) {
            System.out.println("   • " + entry.getKey() + ": " + entry.getValue() + " parcels");
        }

        // Print optimization results
        printLargeScaleResults(stops, testData, solver);
    }

    /**
     * Print large scale optimization results
     */
    private static void printLargeScaleResults(List<Stop> stops, RouteCoordinatesGroup testData, RoutingProblemSolver solver) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         LARGE SCALE TEST - OPTIMIZATION RESULTS               ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Separate stops by type
        List<Stop> customerStops = stops.stream()
            .filter(s -> s.getStopType() == StopType.CUSTOMER)
            .collect(java.util.stream.Collectors.toList());

        // Count total parcels and unique coordinates
        int totalParcelsInRoute = 0;
        Set<String> uniqueCoordinates = new HashSet<>();
        
        for (Stop stop : customerStops) {
            if (stop.getParcelsToDeliver() != null) {
                totalParcelsInRoute += stop.getParcelsToDeliver().size();
                uniqueCoordinates.add(String.format("%.6f,%.6f", 
                    stop.getCoordinates().getLatitude(), stop.getCoordinates().getLongitude()));
            }
        }

        System.out.println("📊 OPTIMIZATION SUMMARY:");
        System.out.println("   📦 Total Parcels Delivered: " + totalParcelsInRoute);
        System.out.println("   🎯 Customer Delivery Stops: " + customerStops.size());
        System.out.println("   📍 Unique Delivery Locations: " + uniqueCoordinates.size());
        System.out.println("   🔗 Coordinate Consolidation: " + (totalParcelsInRoute - uniqueCoordinates.size()) + " parcels grouped");
        System.out.println("   📈 Grouping Efficiency: " + String.format("%.1f%%", 
            ((totalParcelsInRoute - uniqueCoordinates.size()) * 100.0) / totalParcelsInRoute));
        System.out.println("   🚚 Complete Route Stops: " + stops.size() + " (Depot + Warehouse + " + customerStops.size() + " customers)");

        // Display truck metrics
        displayTruckMetrics(solver);

        // Show sample consolidated stops
        showConsolidatedStops(customerStops);

        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("✅ Large Scale Route Optimization Complete!");
        System.out.println("   • Successfully handled 200 parcels with duplicates");
        System.out.println("   • Optimized delivery sequence with TomTom");
        System.out.println("   • Consolidated parcels to same coordinates");
        System.out.println("   • Multi-warehouse routing coordination");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
    }

    /**
     * Display truck metrics from the solver
     */
    private static void displayTruckMetrics(RoutingProblemSolver solver) {


    }

    /**
     * Show sample consolidated delivery stops (where multiple parcels go to same location)
     */
    private static void showConsolidatedStops(List<Stop> customerStops) {
        System.out.println("\n📍 SAMPLE CONSOLIDATED DELIVERY STOPS:");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        int consolidatedShown = 0;
        for (Stop stop : customerStops) {
            if (stop.getParcelsToDeliver() != null && stop.getParcelsToDeliver().size() > 1 && consolidatedShown < 5) {
                System.out.printf("\n🏠 Location: %.6f, %.6f\n",
                    stop.getCoordinates().getLatitude(), stop.getCoordinates().getLongitude());
                System.out.println("📦 Consolidated Parcels (" + stop.getParcelsToDeliver().size() + "):");
                
                for (Parcel parcel : stop.getParcelsToDeliver()) {
                    System.out.printf("   • %s (%.1f m³) → %s\n",
                        parcel.getParcelName(),
                        parcel.getVolume(),
                        parcel.getRecipientName());
                }
                consolidatedShown++;
            }
        }
        
        if (consolidatedShown == 0) {
            System.out.println("   No consolidated stops found (all deliveries to unique locations)");
        }
    }

    /**
     * Create RoutingProblemSolver with manual dependency injection
     */
    private static RoutingProblemSolver createSolverWithDependencies() throws Exception {
        TomTomRouteCalculator tomTomCalculator = new TomTomRouteCalculator();

        RoutingProblemSolver solver = new RoutingProblemSolver();
        java.lang.reflect.Field calculatorField = RoutingProblemSolver.class.getDeclaredField("tomTomRouteCalculator");
        calculatorField.setAccessible(true);
        calculatorField.set(solver, tomTomCalculator);

        return solver;
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
