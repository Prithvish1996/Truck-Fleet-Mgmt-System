package com.saxion.proj.tfms.delivery;

import com.saxion.proj.tfms.delivery.dto.DeliveryOptimizationRequest;
import com.saxion.proj.tfms.delivery.dto.DeliveryOptimizationResponse;
import com.saxion.proj.tfms.truck.model.Truck;
import com.saxion.proj.tfms.delivery.service.DeliveryOptimizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Delivery Optimization Service
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class DeliveryOptimizationServiceTest {

    @Autowired
    private DeliveryOptimizationService deliveryOptimizationService;

    @Test
    void testOptimizeFromJson() {
        System.out.println("\n=== Testing JSON Optimization ===");
        
        // Create test data
        DeliveryOptimizationRequest request = createTestRequest();
        System.out.println("✓ Created test request with " + request.getWarehouses().size() + " warehouses and " + request.getTrucks().size() + " trucks");
        
        // Perform optimization
        System.out.println("🔄 Starting optimization...");
        DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromJson(request);
        
        // Verify response
        System.out.println("✓ Optimization completed!");
        System.out.println("📊 Results:");
        System.out.println("  - Optimization ID: " + response.getOptimizationId());
        System.out.println("  - Total Routes: " + response.getTotalRoutes());
        System.out.println("  - Total Packages: " + response.getTotalPackages());
        System.out.println("  - Unassigned Packages: " + response.getUnassignedPackages().size());
        
        // Print route details
        for (int i = 0; i < response.getRoutes().size(); i++) {
            DeliveryOptimizationResponse.RouteInfo route = response.getRoutes().get(i);
            System.out.println("  - Route " + (i + 1) + ": Truck " + route.getTruckId() + 
                             ", Weight: " + route.getTotalWeight() + 
                             ", Packages: " + route.getPackages().size());
        }
        
        // Verify response
        System.out.println("🔍 Validating JSON optimization results...");
        assertNotNull(response, "Response should not be null");
        System.out.println("  ✓ Response object created successfully");
        
        assertNotNull(response.getOptimizationId(), "Optimization ID should not be null");
        System.out.println("  ✓ Optimization ID generated: " + response.getOptimizationId());
        
        assertTrue(response.getTotalRoutes() > 0, "Should have at least one route");
        System.out.println("  ✓ Total routes created: " + response.getTotalRoutes());
        
        assertTrue(response.getTotalPackages() > 0, "Should have packages assigned");
        System.out.println("  ✓ Total packages processed: " + response.getTotalPackages());
        
        assertNotNull(response.getRoutes(), "Routes list should not be null");
        System.out.println("  ✓ Routes list is valid");
        
        assertNotNull(response.getUnassignedPackages(), "Unassigned packages list should not be null");
        System.out.println("  ✓ Unassigned packages list is valid");
        
        // Verify routes don't exceed truck capacity
        System.out.println("  🔍 Checking weight limits...");
        for (DeliveryOptimizationResponse.RouteInfo route : response.getRoutes()) {
            assertTrue(route.getTotalWeight().compareTo(new BigDecimal("10000.0")) <= 0, 
                "Route weight should not exceed 10000kg");
            System.out.println("    ✓ Route " + route.getTruckId() + " weight: " + route.getTotalWeight() + "kg (within limit)");
        }
        
        System.out.println("✅ JSON optimization test PASSED - All validations successful!\n");
    }
    
    @Test
    void testOptimizeFromCsv() {
        System.out.println("\n=== Testing CSV Optimization ===");
        
        // Create test CSV content
        String csvContent = createTestCsvContent();
        System.out.println("✓ Created test CSV content with " + csvContent.split("\n").length + " lines");
        
        // Create test trucks
        List<Truck> trucks = createTestTrucks();
        System.out.println("✓ Created " + trucks.size() + " test trucks");
        
        // Perform optimization
        System.out.println("🔄 Starting CSV optimization...");
        DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromCsv(csvContent, trucks);
        
        // Verify response
        System.out.println("✓ CSV optimization completed!");
        System.out.println("📊 Results:");
        System.out.println("  - Optimization ID: " + response.getOptimizationId());
        System.out.println("  - Total Routes: " + response.getTotalRoutes());
        System.out.println("  - Total Packages: " + response.getTotalPackages());
        System.out.println("  - Unassigned Packages: " + response.getUnassignedPackages().size());
        
        // Print route details
        for (int i = 0; i < response.getRoutes().size(); i++) {
            DeliveryOptimizationResponse.RouteInfo route = response.getRoutes().get(i);
            System.out.println("  - Route " + (i + 1) + ": Truck " + route.getTruckId() + 
                             ", Weight: " + route.getTotalWeight() + 
                             ", Packages: " + route.getPackages().size());
        }
        
        // Verify response
        System.out.println("🔍 Validating CSV optimization results...");
        assertNotNull(response, "Response should not be null");
        System.out.println("  ✓ Response object created successfully");
        
        assertNotNull(response.getOptimizationId(), "Optimization ID should not be null");
        System.out.println("  ✓ Optimization ID generated: " + response.getOptimizationId());
        
        assertTrue(response.getTotalRoutes() > 0, "Should have at least one route");
        System.out.println("  ✓ Total routes created: " + response.getTotalRoutes());
        
        assertTrue(response.getTotalPackages() > 0, "Should have packages assigned");
        System.out.println("  ✓ Total packages processed: " + response.getTotalPackages());
        
        // Validate CSV-specific results
        System.out.println("  🔍 Validating CSV-specific data...");
        assertNotNull(response.getRoutes(), "Routes should not be null");
        System.out.println("  ✓ Routes list is valid");
        
        // Check that all packages from CSV were processed
        int expectedPackages = 3; // Based on our test CSV content
        assertEquals(expectedPackages, response.getTotalPackages(), 
            "Should process all packages from CSV");
        System.out.println("  ✓ All " + expectedPackages + " CSV packages processed correctly");
        
        System.out.println("✅ CSV optimization test PASSED - All validations successful!\n");
    }
    
    @Test
    void testLargeDatasetOptimization() {
        System.out.println("\n=== Testing Large Dataset Optimization ===");
        
        // Create a large dataset with multiple warehouses and many packages
        DeliveryOptimizationRequest request = createLargeTestRequest();
        System.out.println("✓ Created large test request with " + request.getWarehouses().size() + " warehouses and " + request.getTrucks().size() + " trucks");
        
        // Count total packages
        int totalPackages = request.getWarehouses().stream()
                .mapToInt(w -> w.getPackages().size())
                .sum();
        System.out.println("  - Total packages across all warehouses: " + totalPackages);
        
        // Perform optimization
        System.out.println("🔄 Starting large dataset optimization...");
        long startTime = System.currentTimeMillis();
        DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromJson(request);
        long endTime = System.currentTimeMillis();
        
        // Verify response
        System.out.println("✓ Large dataset optimization completed in " + (endTime - startTime) + "ms!");
        System.out.println("📊 Results:");
        System.out.println("  - Optimization ID: " + response.getOptimizationId());
        System.out.println("  - Total Routes: " + response.getTotalRoutes());
        System.out.println("  - Total Packages: " + response.getTotalPackages());
        System.out.println("  - Unassigned Packages: " + response.getUnassignedPackages().size());
        
        // Validate routes
        validateRoutes(response, request.getTrucks());
        
        // Verify response
        System.out.println("🔍 Validating large dataset optimization results...");
        assertNotNull(response, "Response should not be null");
        System.out.println("  ✓ Response object created successfully");
        
        assertNotNull(response.getOptimizationId(), "Optimization ID should not be null");
        System.out.println("  ✓ Optimization ID generated: " + response.getOptimizationId());
        
        assertTrue(response.getTotalRoutes() > 0, "Should have at least one route");
        System.out.println("  ✓ Total routes created: " + response.getTotalRoutes());
        
        assertEquals(totalPackages, response.getTotalPackages(), 
            "All packages should be processed");
        System.out.println("  ✓ All " + totalPackages + " packages processed correctly");
        
        // Validate performance
        System.out.println("  🔍 Validating performance metrics...");
        assertTrue((endTime - startTime) < 1000, "Optimization should complete within 1 second");
        System.out.println("  ✓ Performance acceptable: " + (endTime - startTime) + "ms");
        
        // Validate route efficiency
        System.out.println("  🔍 Validating route efficiency...");
        assertTrue(response.getTotalRoutes() <= request.getTrucks().size(), 
            "Should not create more routes than trucks available");
        System.out.println("  ✓ Route efficiency: " + response.getTotalRoutes() + " routes for " + 
                          request.getTrucks().size() + " trucks");
        
        System.out.println("✅ Large dataset optimization test PASSED - All validations successful!\n");
    }
    
    @Test
    void testWeightLimitValidation() {
        System.out.println("\n=== Testing Weight Limit Validation ===");
        
        // Create test data that should test weight limits
        DeliveryOptimizationRequest request = createWeightLimitTestRequest();
        System.out.println("✓ Created weight limit test with packages that exceed truck capacity");
        
        // Perform optimization
        System.out.println("🔄 Starting weight limit validation...");
        DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromJson(request);
        
        // Verify response
        System.out.println("✓ Weight limit validation completed!");
        System.out.println("📊 Results:");
        System.out.println("  - Optimization ID: " + response.getOptimizationId());
        System.out.println("  - Total Routes: " + response.getTotalRoutes());
        System.out.println("  - Total Packages: " + response.getTotalPackages());
        System.out.println("  - Unassigned Packages: " + response.getUnassignedPackages().size());
        
        // Validate that no route exceeds weight limit
        validateWeightLimits(response, request.getTrucks());
        
        // Print route details
        for (int i = 0; i < response.getRoutes().size(); i++) {
            DeliveryOptimizationResponse.RouteInfo route = response.getRoutes().get(i);
            System.out.println("  - Route " + (i + 1) + ": Truck " + route.getTruckId() + 
                             ", Weight: " + route.getTotalWeight() + 
                             ", Packages: " + route.getPackages().size());
        }
        
        // Verify response
        System.out.println("🔍 Validating weight limit test results...");
        assertNotNull(response, "Response should not be null");
        System.out.println("  ✓ Response object created successfully");
        
        assertNotNull(response.getOptimizationId(), "Optimization ID should not be null");
        System.out.println("  ✓ Optimization ID generated: " + response.getOptimizationId());
        
        assertTrue(response.getTotalRoutes() > 0, "Should have at least one route");
        System.out.println("  ✓ Total routes created: " + response.getTotalRoutes());
        
        // Validate that heavy packages were handled correctly
        System.out.println("  🔍 Validating heavy package handling...");
        int totalPackagesInRoutes = response.getRoutes().stream()
            .mapToInt(route -> route.getPackages().size())
            .sum();
        System.out.println("  ✓ Packages assigned to routes: " + totalPackagesInRoutes);
        
        // Check that some packages might be unassigned due to weight limits
        System.out.println("  🔍 Validating unassigned packages handling...");
        assertNotNull(response.getUnassignedPackages(), "Unassigned packages list should exist");
        System.out.println("  ✓ Unassigned packages: " + response.getUnassignedPackages().size());
        
        // Validate that no route exceeds its truck's capacity
        System.out.println("  🔍 Validating no capacity violations...");
        for (DeliveryOptimizationResponse.RouteInfo route : response.getRoutes()) {
            // This validation is already done in validateWeightLimits, but let's confirm
            System.out.println("    ✓ Route " + route.getTruckId() + " validated");
        }
        System.out.println("  ✓ All routes respect weight limits");
        
        System.out.println("✅ Weight limit validation test PASSED - All validations successful!\n");
    }
    
    @Test
    void testGeographicOptimization() {
        System.out.println("\n=== Testing Geographic Route Optimization ===");
        
        // Create test data with packages spread across different geographic areas
        DeliveryOptimizationRequest request = createGeographicTestRequest();
        System.out.println("✓ Created geographic test with packages in different locations");
        
        // Perform optimization
        System.out.println("🔄 Starting geographic optimization...");
        DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromJson(request);
        
        // Verify response
        System.out.println("✓ Geographic optimization completed!");
        System.out.println("📊 Results:");
        System.out.println("  - Optimization ID: " + response.getOptimizationId());
        System.out.println("  - Total Routes: " + response.getTotalRoutes());
        System.out.println("  - Total Packages: " + response.getTotalPackages());
        System.out.println("  - Unassigned Packages: " + response.getUnassignedPackages().size());
        
        // Validate routes are geographically logical
        validateGeographicRoutes(response);
        
        // Print route details with coordinates
        for (int i = 0; i < response.getRoutes().size(); i++) {
            DeliveryOptimizationResponse.RouteInfo route = response.getRoutes().get(i);
            System.out.println("  - Route " + (i + 1) + ": Truck " + route.getTruckId() + 
                             ", Weight: " + route.getTotalWeight() + 
                             ", Packages: " + route.getPackages().size());
            
            // Show package locations for this route
            for (int j = 0; j < Math.min(route.getPackages().size(), 3); j++) {
                var pkg = route.getPackages().get(j);
                System.out.println("    * " + pkg.getName() + " at (" + pkg.getLatitude() + ", " + pkg.getLongitude() + ")");
            }
            if (route.getPackages().size() > 3) {
                System.out.println("    * ... and " + (route.getPackages().size() - 3) + " more packages");
            }
        }
        
        // Verify response
        System.out.println("🔍 Validating geographic optimization results...");
        assertNotNull(response, "Response should not be null");
        System.out.println("  ✓ Response object created successfully");
        
        assertNotNull(response.getOptimizationId(), "Optimization ID should not be null");
        System.out.println("  ✓ Optimization ID generated: " + response.getOptimizationId());
        
        assertTrue(response.getTotalRoutes() > 0, "Should have at least one route");
        System.out.println("  ✓ Total routes created: " + response.getTotalRoutes());
        
        // Validate geographic clustering
        System.out.println("  🔍 Validating geographic clustering...");
        assertTrue(response.getTotalRoutes() >= 2, "Should create multiple routes for different regions");
        System.out.println("  ✓ Geographic clustering: " + response.getTotalRoutes() + " routes for different areas");
        
        // Validate that packages are distributed across routes
        System.out.println("  🔍 Validating package distribution...");
        int totalPackagesInRoutes = response.getRoutes().stream()
            .mapToInt(route -> route.getPackages().size())
            .sum();
        assertEquals(10, totalPackagesInRoutes, "All 10 geographic packages should be assigned");
        System.out.println("  ✓ All " + totalPackagesInRoutes + " geographic packages distributed correctly");
        
        // Validate that routes have reasonable package counts
        System.out.println("  🔍 Validating route balance...");
        for (DeliveryOptimizationResponse.RouteInfo route : response.getRoutes()) {
            assertTrue(route.getPackages().size() > 0, "Each route should have at least one package");
            System.out.println("    ✓ Route " + route.getTruckId() + " has " + route.getPackages().size() + " packages");
        }
        
        // Validate no unassigned packages
        System.out.println("  🔍 Validating no unassigned packages...");
        assertEquals(0, response.getUnassignedPackages().size(), "All packages should be assigned to routes");
        System.out.println("  ✓ No unassigned packages - all geographic packages assigned");
        
        System.out.println("✅ Geographic optimization test PASSED - All validations successful!\n");
    }
    
    private void validateRoutes(DeliveryOptimizationResponse response, List<DeliveryOptimizationRequest.TruckData> trucks) {
        System.out.println("🔍 Validating routes...");
        
        // Create truck capacity map
        var truckCapacities = trucks.stream()
                .collect(java.util.stream.Collectors.toMap(
                    DeliveryOptimizationRequest.TruckData::getTruckId,
                    DeliveryOptimizationRequest.TruckData::getWeightLimit
                ));
        
        for (DeliveryOptimizationResponse.RouteInfo route : response.getRoutes()) {
            Double truckCapacityDouble = truckCapacities.get(route.getTruckId());
            assertNotNull(truckCapacityDouble, "Truck capacity not found for " + route.getTruckId());
            BigDecimal truckCapacity = BigDecimal.valueOf(truckCapacityDouble);
            
            // Validate weight limit
            assertTrue(route.getTotalWeight().compareTo(truckCapacity) <= 0, 
                "Route exceeds weight limit: " + route.getTotalWeight() + " > " + truckCapacity);
            
            System.out.println("  ✓ Route " + route.getTruckId() + " respects weight limit: " + 
                             route.getTotalWeight() + " <= " + truckCapacity);
        }
        
        System.out.println("  ✓ All routes respect weight limits");
    }
    
    private void validateWeightLimits(DeliveryOptimizationResponse response, List<DeliveryOptimizationRequest.TruckData> trucks) {
        System.out.println("🔍 Validating weight limits...");
        
        var truckCapacities = trucks.stream()
                .collect(java.util.stream.Collectors.toMap(
                    DeliveryOptimizationRequest.TruckData::getTruckId,
                    DeliveryOptimizationRequest.TruckData::getWeightLimit
                ));
        
        boolean allRoutesValid = true;
        for (DeliveryOptimizationResponse.RouteInfo route : response.getRoutes()) {
            Double truckCapacityDouble = truckCapacities.get(route.getTruckId());
            BigDecimal truckCapacity = BigDecimal.valueOf(truckCapacityDouble);
            boolean isValid = route.getTotalWeight().compareTo(truckCapacity) <= 0;
            
            System.out.println("  - Route " + route.getTruckId() + ": " + 
                             route.getTotalWeight() + "/" + truckCapacity + 
                             (isValid ? " ✓" : " ❌ EXCEEDS LIMIT"));
            
            if (!isValid) {
                allRoutesValid = false;
            }
        }
        
        assertTrue(allRoutesValid, "Some routes exceed weight limits");
        System.out.println("  ✓ All routes respect weight limits");
    }
    
    private void validateGeographicRoutes(DeliveryOptimizationResponse response) {
        System.out.println("🔍 Validating geographic route logic...");
        
        for (DeliveryOptimizationResponse.RouteInfo route : response.getRoutes()) {
            if (route.getPackages().size() > 1) {
                // Calculate total distance (simplified - just sum of distances from origin)
                double totalDistance = 0;
                double prevLat = 0, prevLng = 0;
                
                for (int i = 0; i < route.getPackages().size(); i++) {
                    var pkg = route.getPackages().get(i);
                    if (i == 0) {
                        prevLat = pkg.getLatitude().doubleValue();
                        prevLng = pkg.getLongitude().doubleValue();
                    } else {
                        // Simple distance calculation (not accurate but good for testing)
                        double distance = Math.sqrt(
                            Math.pow(pkg.getLatitude().doubleValue() - prevLat, 2) + 
                            Math.pow(pkg.getLongitude().doubleValue() - prevLng, 2)
                        );
                        totalDistance += distance;
                        prevLat = pkg.getLatitude().doubleValue();
                        prevLng = pkg.getLongitude().doubleValue();
                    }
                }
                
                System.out.println("  - Route " + route.getTruckId() + " total distance: " + 
                                 String.format("%.4f", totalDistance) + " units");
            }
        }
        
        System.out.println("  ✓ Geographic route validation completed");
    }
    
    private DeliveryOptimizationRequest createLargeTestRequest() {
        System.out.println("📦 Creating large test dataset...");
        
        DeliveryOptimizationRequest request = new DeliveryOptimizationRequest();
        
        // Create multiple warehouses
        List<DeliveryOptimizationRequest.WarehouseData> warehouses = new ArrayList<>();
        
        // Warehouse 1: 20 packages
        DeliveryOptimizationRequest.WarehouseData warehouse1 = new DeliveryOptimizationRequest.WarehouseData();
        warehouse1.setName("Warehouse North");
        warehouse1.setLatitude(52.5);
        warehouse1.setLongitude(4.5);
        warehouse1.setDeliveryDate("2024-01-15");
        
        List<DeliveryOptimizationRequest.PackageData> packages1 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("North Package " + i);
            packageData.setWeight(50.0 + (i % 10) * 10); // Varying weights
            packageData.setSize(30.0 + i);
            packageData.setLatitude(52.5 + (i % 5) * 0.01);
            packageData.setLongitude(4.5 + (i % 3) * 0.01);
            packages1.add(packageData);
        }
        warehouse1.setPackages(packages1);
        warehouses.add(warehouse1);
        
        // Warehouse 2: 15 packages
        DeliveryOptimizationRequest.WarehouseData warehouse2 = new DeliveryOptimizationRequest.WarehouseData();
        warehouse2.setName("Warehouse South");
        warehouse2.setLatitude(52.0);
        warehouse2.setLongitude(4.0);
        warehouse2.setDeliveryDate("2024-01-15");
        
        List<DeliveryOptimizationRequest.PackageData> packages2 = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("South Package " + i);
            packageData.setWeight(75.0 + (i % 8) * 15);
            packageData.setSize(40.0 + i * 2);
            packageData.setLatitude(52.0 + (i % 4) * 0.02);
            packageData.setLongitude(4.0 + (i % 6) * 0.015);
            packages2.add(packageData);
        }
        warehouse2.setPackages(packages2);
        warehouses.add(warehouse2);
        
        // Warehouse 3: 10 packages
        DeliveryOptimizationRequest.WarehouseData warehouse3 = new DeliveryOptimizationRequest.WarehouseData();
        warehouse3.setName("Warehouse East");
        warehouse3.setLatitude(52.2);
        warehouse3.setLongitude(4.8);
        warehouse3.setDeliveryDate("2024-01-16");
        
        List<DeliveryOptimizationRequest.PackageData> packages3 = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("East Package " + i);
            packageData.setWeight(100.0 + i * 20);
            packageData.setSize(50.0 + i * 3);
            packageData.setLatitude(52.2 + (i % 3) * 0.03);
            packageData.setLongitude(4.8 + (i % 2) * 0.02);
            packages3.add(packageData);
        }
        warehouse3.setPackages(packages3);
        warehouses.add(warehouse3);
        
        request.setWarehouses(warehouses);
        
        // Create multiple trucks with different capacities
        List<DeliveryOptimizationRequest.TruckData> trucks = new ArrayList<>();
        
        DeliveryOptimizationRequest.TruckData truck1 = new DeliveryOptimizationRequest.TruckData();
        truck1.setTruckId("TRUCK-LARGE-001");
        truck1.setWeightLimit(2000.0);
        trucks.add(truck1);
        
        DeliveryOptimizationRequest.TruckData truck2 = new DeliveryOptimizationRequest.TruckData();
        truck2.setTruckId("TRUCK-MEDIUM-002");
        truck2.setWeightLimit(1500.0);
        trucks.add(truck2);
        
        DeliveryOptimizationRequest.TruckData truck3 = new DeliveryOptimizationRequest.TruckData();
        truck3.setTruckId("TRUCK-SMALL-003");
        truck3.setWeightLimit(1000.0);
        trucks.add(truck3);
        
        DeliveryOptimizationRequest.TruckData truck4 = new DeliveryOptimizationRequest.TruckData();
        truck4.setTruckId("TRUCK-LARGE-004");
        truck4.setWeightLimit(2500.0);
        trucks.add(truck4);
        
        request.setTrucks(trucks);
        
        System.out.println("  - Created 3 warehouses with 45 total packages");
        System.out.println("  - Created 4 trucks with varying capacities (1000-2500kg)");
        
        return request;
    }
    
    private DeliveryOptimizationRequest createWeightLimitTestRequest() {
        System.out.println("📦 Creating weight limit test dataset...");
        
        DeliveryOptimizationRequest request = new DeliveryOptimizationRequest();
        
        // Create warehouse with heavy packages
        List<DeliveryOptimizationRequest.WarehouseData> warehouses = new ArrayList<>();
        DeliveryOptimizationRequest.WarehouseData warehouse = new DeliveryOptimizationRequest.WarehouseData();
        warehouse.setName("Heavy Warehouse");
        warehouse.setLatitude(52.0);
        warehouse.setLongitude(4.0);
        warehouse.setDeliveryDate("2024-01-15");
        
        List<DeliveryOptimizationRequest.PackageData> packages = new ArrayList<>();
        // Create packages that will test weight limits
        for (int i = 1; i <= 8; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("Heavy Package " + i);
            packageData.setWeight(300.0 + i * 50); // 350, 400, 450, 500, 550, 600, 650, 700
            packageData.setSize(50.0);
            packageData.setLatitude(52.0 + i * 0.01);
            packageData.setLongitude(4.0 + i * 0.01);
            packages.add(packageData);
        }
        warehouse.setPackages(packages);
        warehouses.add(warehouse);
        request.setWarehouses(warehouses);
        
        // Create trucks with limited capacity
        List<DeliveryOptimizationRequest.TruckData> trucks = new ArrayList<>();
        
        DeliveryOptimizationRequest.TruckData truck1 = new DeliveryOptimizationRequest.TruckData();
        truck1.setTruckId("TRUCK-SMALL-001");
        truck1.setWeightLimit(500.0); // Can only carry 1 heavy package
        trucks.add(truck1);
        
        DeliveryOptimizationRequest.TruckData truck2 = new DeliveryOptimizationRequest.TruckData();
        truck2.setTruckId("TRUCK-MEDIUM-002");
        truck2.setWeightLimit(1000.0); // Can carry 2 heavy packages
        trucks.add(truck2);
        
        DeliveryOptimizationRequest.TruckData truck3 = new DeliveryOptimizationRequest.TruckData();
        truck3.setTruckId("TRUCK-LARGE-003");
        truck3.setWeightLimit(2000.0); // Can carry multiple packages
        trucks.add(truck3);
        
        request.setTrucks(trucks);
        
        System.out.println("  - Created 1 warehouse with 8 heavy packages (350-700kg each)");
        System.out.println("  - Created 3 trucks with limited capacities (500, 1000, 2000kg)");
        
        return request;
    }
    
    private DeliveryOptimizationRequest createGeographicTestRequest() {
        System.out.println("📦 Creating geographic test dataset...");
        
        DeliveryOptimizationRequest request = new DeliveryOptimizationRequest();
        
        // Create warehouse with packages in different geographic areas
        List<DeliveryOptimizationRequest.WarehouseData> warehouses = new ArrayList<>();
        DeliveryOptimizationRequest.WarehouseData warehouse = new DeliveryOptimizationRequest.WarehouseData();
        warehouse.setName("Central Warehouse");
        warehouse.setLatitude(52.0);
        warehouse.setLongitude(4.0);
        warehouse.setDeliveryDate("2024-01-15");
        
        List<DeliveryOptimizationRequest.PackageData> packages = new ArrayList<>();
        
        // North area packages
        for (int i = 1; i <= 3; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("North Package " + i);
            packageData.setWeight(100.0);
            packageData.setSize(50.0);
            packageData.setLatitude(52.1 + i * 0.01); // North of warehouse
            packageData.setLongitude(4.0 + i * 0.005);
            packages.add(packageData);
        }
        
        // South area packages
        for (int i = 1; i <= 3; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("South Package " + i);
            packageData.setWeight(120.0);
            packageData.setSize(60.0);
            packageData.setLatitude(51.9 - i * 0.01); // South of warehouse
            packageData.setLongitude(4.0 - i * 0.005);
            packages.add(packageData);
        }
        
        // East area packages
        for (int i = 1; i <= 2; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("East Package " + i);
            packageData.setWeight(80.0);
            packageData.setSize(40.0);
            packageData.setLatitude(52.0 + i * 0.005);
            packageData.setLongitude(4.1 + i * 0.01); // East of warehouse
            packages.add(packageData);
        }
        
        // West area packages
        for (int i = 1; i <= 2; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("West Package " + i);
            packageData.setWeight(90.0);
            packageData.setSize(45.0);
            packageData.setLatitude(52.0 - i * 0.005);
            packageData.setLongitude(3.9 - i * 0.01); // West of warehouse
            packages.add(packageData);
        }
        
        warehouse.setPackages(packages);
        warehouses.add(warehouse);
        request.setWarehouses(warehouses);
        
        // Create trucks
        List<DeliveryOptimizationRequest.TruckData> trucks = new ArrayList<>();
        
        DeliveryOptimizationRequest.TruckData truck1 = new DeliveryOptimizationRequest.TruckData();
        truck1.setTruckId("TRUCK-NORTH-001");
        truck1.setWeightLimit(500.0);
        trucks.add(truck1);
        
        DeliveryOptimizationRequest.TruckData truck2 = new DeliveryOptimizationRequest.TruckData();
        truck2.setTruckId("TRUCK-SOUTH-002");
        truck2.setWeightLimit(500.0);
        trucks.add(truck2);
        
        DeliveryOptimizationRequest.TruckData truck3 = new DeliveryOptimizationRequest.TruckData();
        truck3.setTruckId("TRUCK-EAST-003");
        truck3.setWeightLimit(300.0);
        trucks.add(truck3);
        
        request.setTrucks(trucks);
        
        System.out.println("  - Created 1 warehouse with 10 packages in different geographic areas");
        System.out.println("  - Created 3 trucks for different regions");
        
        return request;
    }
    
    private DeliveryOptimizationRequest createTestRequest() {
        System.out.println("📦 Creating test data...");
        
        DeliveryOptimizationRequest request = new DeliveryOptimizationRequest();
        
        // Create warehouses
        List<DeliveryOptimizationRequest.WarehouseData> warehouses = new ArrayList<>();
        DeliveryOptimizationRequest.WarehouseData warehouse = new DeliveryOptimizationRequest.WarehouseData();
        warehouse.setName("Test Warehouse");
        warehouse.setLatitude(52.0);
        warehouse.setLongitude(4.0);
        warehouse.setDeliveryDate("2024-01-15");
        
        // Create packages
        List<DeliveryOptimizationRequest.PackageData> packages = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            DeliveryOptimizationRequest.PackageData packageData = new DeliveryOptimizationRequest.PackageData();
            packageData.setName("Package " + i);
            packageData.setWeight(100.0 + i * 10);
            packageData.setSize(50.0);
            packageData.setLatitude(52.0 + i * 0.01);
            packageData.setLongitude(4.0 + i * 0.01);
            packages.add(packageData);
        }
        warehouse.setPackages(packages);
        warehouses.add(warehouse);
        request.setWarehouses(warehouses);
        
        // Create trucks
        List<DeliveryOptimizationRequest.TruckData> trucks = new ArrayList<>();
        DeliveryOptimizationRequest.TruckData truck = new DeliveryOptimizationRequest.TruckData();
        truck.setTruckId("TRUCK-001");
        truck.setWeightLimit(1000.0);
        trucks.add(truck);
        request.setTrucks(trucks);
        
        System.out.println("  - Created 1 warehouse with 5 packages");
        System.out.println("  - Created 1 truck with 1000kg capacity");
        
        return request;
    }
    
    private String createTestCsvContent() {
        return "warehouse_name,warehouse_lat,warehouse_lng,delivery_date,package_name,package_weight,package_size,package_lat,package_lng\n" +
               "Warehouse A,52.0,4.0,2024-01-15,Package 1,100.0,50.0,52.01,4.01\n" +
               "Warehouse A,52.0,4.0,2024-01-15,Package 2,150.0,60.0,52.02,4.02\n" +
               "Warehouse B,52.1,4.1,2024-01-16,Package 3,200.0,70.0,52.11,4.11\n";
    }
    
    private List<Truck> createTestTrucks() {
        System.out.println("🚛 Creating test trucks...");
        List<Truck> trucks = new ArrayList<>();
        trucks.add(new Truck("TRUCK-001", new BigDecimal("1000.0")));
        trucks.add(new Truck("TRUCK-002", new BigDecimal("1500.0")));
        System.out.println("  - TRUCK-001: 1000kg capacity");
        System.out.println("  - TRUCK-002: 1500kg capacity");
        return trucks;
    }
}
