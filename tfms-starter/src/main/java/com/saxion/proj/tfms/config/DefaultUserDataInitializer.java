package com.saxion.proj.tfms.config;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.commons.constants.TruckType;
import com.saxion.proj.tfms.commons.model.*;
import com.saxion.proj.tfms.planner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.saxion.proj.tfms.commons.model.UserDao.UserType;
import com.saxion.proj.tfms.auth.repository.AuthUserRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Component to initialize default users in the database for development and testing
 */
@Component
public class DefaultUserDataInitializer {

    @Autowired
    private AuthUserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private WarehouseRepository wareHouseRepository;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    /**
     * Initialize default users after the application context is fully loaded
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaultUsers() {
        
        // Create default admin user
        createUserIfNotExists("admin@tfms.com", "admin", "admin123", UserType.ADMIN);
        
        // Create default driver user  
        createUserIfNotExists("driver@tfms.com", "driver", "driver123", UserType.DRIVER);
        createUserIfNotExists("driver2@tfms.com", "driver2", "driver123", UserType.DRIVER);
        createUserIfNotExists("driver3@tfms.com", "driver3", "driver123", UserType.DRIVER);

        // Create default planner user
        createUserIfNotExists("planner@tfms.com", "planner", "planner123", UserType.PLANNER);
        
        // Create default test user
        createUserIfNotExists("test@example.com", "testuser", "password123", UserType.DRIVER);

        // Create default warehouse
        createDepotIfNotExists("Main Depot", 500.0);

        // Create 200 parcels from 10 different warehouses (20 parcels per warehouse)
        create200ParcelsFrom10Warehouses();

        // Create default drivers
        createDefaultDriver("driver@tfms.com", "Deventer", "22222");
        createDefaultDriver("driver2@tfms.com", "Utrecht", "44444");
        createDefaultDriver("driver3@tfms.com", "Arnhem", "66666");

        // Create 10 trucks with varying capacities
        create10Trucks();

        // Create default routes for driver ID 2
        //createDefaultRoutesForDriver(2L);

        System.out.println("Default users initialization completed.");
    }

    /**
     * Create a user if it doesn't already exist
     */
    private void createUserIfNotExists(String email, String username, String password, UserType userType) {
        if (!userRepository.existsByEmail(email)) {
            UserDao user = new UserDao();
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setUserType(userType);
            user.setActive(true);
            user.setCreatedAt(ZonedDateTime.now());
            user.setUpdatedAt(ZonedDateTime.now());
            
            userRepository.save(user);
            System.out.println("Default user created: " + email + " (" + userType + ")");
        }
    }

    //Create default depot
    private void createDepotIfNotExists(String name, Double capacity) {
        String locationPostcode = "11111";
        LocationDao location = new LocationDao();
        if (!locationRepository.existsByPostalCode(locationPostcode)) {
            location.setPostalCode(locationPostcode);
            location.setAddress("Netherlands");
            location.setCity("Deventer");
            location.setLatitude(10.0);
            location.setLongitude(20.0);
            locationRepository.save(location);
            System.out.println("Default depot created: " + name + " (" + location + ")");
        }

        if (!depotRepository.existsByName(name)) {
            DepotDao depot = new DepotDao();
            depot.setName(name);
            depot.setLocation(location);
            depot.setCapacity(capacity);
            depotRepository.save(depot);
            System.out.println("Default depot created: " + name + " (" + location + ")");
        }
    }

    //  Create Default Parcel
    private void createDefaultWarehouseAndParcel(String parcelName, String city,String warehouseName,String warehousePostcode) {

        // Create location for warehouse if missing
        LocationDao warehouseLocation = locationRepository
                .findByPostalCode(warehousePostcode)
                .orElseGet(() -> {
                    LocationDao loc = new LocationDao();
                    loc.setPostalCode(warehousePostcode);
                    loc.setCity(city);
                    loc.setAddress("Warehouse Street 5");
                    loc.setLatitude(52.25);
                    loc.setLongitude(6.16);
                    return locationRepository.save(loc);
                });

        // Create warehouse if missing
        WareHouseDao warehouse = wareHouseRepository
                .findByName(warehouseName)
                .orElseGet(() -> {
                    WareHouseDao w = new WareHouseDao();
                    w.setName(warehouseName);
                    w.setLocation(warehouseLocation);
                    return wareHouseRepository.save(w);
                });

        // Create delivery location
        LocationDao deliveryLocation = locationRepository
                .findByPostalCode("33333")
                .orElseGet(() -> {
                    LocationDao loc = new LocationDao();
                    loc.setPostalCode("33333");
                    loc.setCity("Amsterdam");
                    loc.setAddress("Delivery Lane 12");
                    loc.setLatitude(52.37);
                    loc.setLongitude(4.90);
                    return locationRepository.save(loc);
                });

        // Create default parcel if missing
        if (!parcelRepository.existsByName(parcelName)) {
            ParcelDao parcel = new ParcelDao();
            parcel.setName(parcelName);
            parcel.setWarehouse(warehouse);
            parcel.setDeliveryLocation(deliveryLocation);
            parcel.setWeight(5.5);
            parcel.setVolume(0.3);
            parcel.setStatus(StatusEnum.PENDING);
            parcel.setDeliveryInstructions("Leave at the front door");
            parcel.setRecipientName("John Doe");
            parcel.setRecipientPhone("+31612345678");
            parcel.setPlannedDeliveryDate(ZonedDateTime.now().plusDays(1));

            parcelRepository.save(parcel);
        }
    }

    // Create Default Driver
    private void createDefaultDriver(String driverEmail, String city, String postCode) {

        // Retrieve existing user account for driver
        Optional<UserDao> userOpt = userRepository.findByEmail(driverEmail);

        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Driver user profile not found: driver@tfms.com");
        }

        UserDao user = userOpt.get();

        // Create driver location if missing
        LocationDao baseLocation = locationRepository
                .findByPostalCode("44444")
                .orElseGet(() -> {
                    LocationDao loc = new LocationDao();
                    loc.setPostalCode(postCode);
                    loc.setCity(city);
                    loc.setAddress("Driver Base 1");
                    loc.setLatitude(52.09);
                    loc.setLongitude(5.12);
                    return locationRepository.save(loc);
                });

        // Create driver if missing
        if (!driverRepository.existsByUser(user)) {
            DriverDao driver = new DriverDao();
            driver.setUser(user);
            driver.setLocation(baseLocation);
            driver.setIsAvailable(true);
            driverRepository.save(driver);
            System.out.println("Default Driver created: " + driverEmail);
        }
    }

    // Create Default Truck
    private void createDefaultTruck(String plateNumber, TruckType type, double volume) {
        // String  = "TRK-001";

        if (!truckRepository.existsByPlateNumber(plateNumber)) {
            TruckDao truck = new TruckDao();
            truck.setPlateNumber(plateNumber);
            truck.setType(type);
            truck.setMake("Volvo");
            truck.setLastServiceDate(LocalDate.now().minusMonths(3));
            truck.setLastServicedBy("Mike");
            truck.setVolume(volume);
            truck.setIsAvailable(true);
            truckRepository.save(truck);
            System.out.println("Default Truck created: " + plateNumber);
        }
    }

    /**
     * Create 10 trucks with varying capacities and types
     */
    private void create10Trucks() {
        System.out.println("Creating 10 trucks with varying capacities...");

        // Define 10 trucks with different sizes and capacities
        Object[][] truckData = {
            {"TRK-001", TruckType.SMALL, 15.0, "Ford Transit"},
            {"TRK-002", TruckType.SMALL, 18.0, "Mercedes Sprinter"},
            {"TRK-003", TruckType.SMALL, 20.0, "Iveco Daily"},
            {"TRK-004", TruckType.MEDIUM, 35.0, "MAN TGL"},
            {"TRK-005", TruckType.MEDIUM, 40.0, "DAF LF"},
            {"TRK-006", TruckType.MEDIUM, 45.0, "Volvo FL"},
            {"TRK-007", TruckType.LARGE, 65.0, "Scania P-Series"},
            {"TRK-008", TruckType.LARGE, 75.0, "Mercedes Actros"},
            {"TRK-009", TruckType.LARGE, 85.0, "Volvo FH"},
            {"TRK-010", TruckType.LARGE, 90.0, "DAF XF"}
        };

        for (Object[] truck : truckData) {
            String plateNumber = (String) truck[0];
            TruckType type = (TruckType) truck[1];
            Double volume = (Double) truck[2];
            String make = (String) truck[3];

            if (!truckRepository.existsByPlateNumber(plateNumber)) {
                TruckDao truckDao = new TruckDao();
                truckDao.setPlateNumber(plateNumber);
                truckDao.setType(type);
                truckDao.setMake(make);
                truckDao.setLastServiceDate(LocalDate.now().minusMonths(1 + (plateNumber.charAt(4) - '0')));
                truckDao.setLastServicedBy("Service Team " + (plateNumber.charAt(4) - '0'));
                truckDao.setVolume(volume);
                truckDao.setIsAvailable(true);
                truckRepository.save(truckDao);

                System.out.println("Created truck: " + plateNumber + " (" + type + ", " + volume + "m³, " + make + ")");
            }
        }

        System.out.println("✅ Successfully created 10 trucks with varying capacities!");
    }

    // Create Default Routes for Driver
    private void createDefaultRoutesForDriver(Long driverId) {
        // Get driver with ID 2
        Optional<DriverDao> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            System.out.println("Driver with ID " + driverId + " not found. Skipping route creation.");
            return;
        }
        DriverDao driver = driverOpt.get();

        // Get depot (Main Depot)
        Optional<DepotDao> depotOpt = depotRepository.findByName("Main Depot");
        if (depotOpt.isEmpty()) {
            System.out.println("Main Depot not found. Skipping route creation.");
            return;
        }
        DepotDao depot = depotOpt.get();

        // Get a truck (use first available truck)
        Optional<TruckDao> truckOpt = truckRepository.findAll().stream()
                .filter(t -> t.getIsAvailable() != null && t.getIsAvailable())
                .findFirst();
        TruckDao truck = truckOpt.orElse(null);

        // Check if routes already exist for this driver
        long existingRoutesCount = routeRepository.findAllByDriverIdAndStatus(driverId, StatusEnum.ASSIGNED).size();
        if (existingRoutesCount > 0) {
            System.out.println("Routes already exist for driver ID " + driverId + ". Skipping route creation.");
            return;
        }

        // Create 3 sample routes: 2 for today, 1 for tomorrow
        ZonedDateTime now = ZonedDateTime.now();
        
        for (int i = 1; i <= 3; i++) {
            RouteDao route = new RouteDao();
            route.setDriver(driver);
            route.setTruck(truck);
            // Only assign depot to the first route (OneToOne constraint - only one route per depot)
            route.setDepot(i == 1 ? depot : null);
            route.setTotalDistance(50L + (i * 10L)); // 60, 70, 80 km
            route.setTotalTransportTime(120L + (i * 30L)); // 150, 180, 210 minutes
            route.setNote("Seeded route " + i + " for driver ID " + driverId);
            route.setStatus(StatusEnum.ASSIGNED); // Important: must be ASSIGNED
            
            // Set dates and times: each route gets a distinct start time with full date (day, month, year)
            ZonedDateTime routeStartTime;
            if (i == 1) {
                // Route 1: today at 8:00 AM
                routeStartTime = now.toLocalDate().atTime(8, 0).atZone(now.getZone());
            } else if (i == 2) {
                // Route 2: today at 10:00 AM
                routeStartTime = now.toLocalDate().atTime(10, 0).atZone(now.getZone());
            } else {
                // Route 3: tomorrow at 8:00 AM
                routeStartTime = now.toLocalDate().plusDays(1).atTime(8, 0).atZone(now.getZone());
            }
            
            route.setStartTime(routeStartTime);
            route.setScheduleDate(routeStartTime);
            
            route.setDuration("4 hours");
            
            route = routeRepository.save(route);
            
            // Create stops for this route
            List<RouteStopDao> stops = createStopsForRoute(route, i);
            route.setStops(stops);
            routeRepository.save(route);
            
            System.out.println("Default Route " + i + " created for driver ID " + driverId + " with " + stops.size() + " stops");
        }
    }

    // Create stops for a route
    private List<RouteStopDao> createStopsForRoute(RouteDao route, int routeNumber) {
        List<RouteStopDao> stops = new ArrayList<>();
        
        // Get or create warehouse for parcels
        WareHouseDao warehouse = wareHouseRepository
                .findByName("Central Warehouse")
                .orElseGet(() -> {
                    // Create warehouse location if needed
                    LocationDao warehouseLocation = locationRepository
                            .findByPostalCode("22222")
                            .orElseGet(() -> {
                                LocationDao loc = new LocationDao();
                                loc.setPostalCode("22222");
                                loc.setCity("Deventer");
                                loc.setAddress("Warehouse Street 5");
                                loc.setLatitude(52.25);
                                loc.setLongitude(6.16);
                                return locationRepository.save(loc);
                            });
                    
                    WareHouseDao w = new WareHouseDao();
                    w.setName("Central Warehouse");
                    w.setLocation(warehouseLocation);
                    return wareHouseRepository.save(w);
                });
        
        // Create stops: DEPOT -> WAREHOUSE -> multiple CUSTOMER -> DEPOT
        // Minimum: 1 DEPOT + 1 WAREHOUSE + 2 CUSTOMER + 1 DEPOT = 5 stops
        // Route 1: 5 stops (2 customers), Route 2: 6 stops (3 customers), Route 3: 5 stops (2 customers)
        int numberOfCustomerStops = 2 + (routeNumber % 2); // 2 customers for route 1, 3 for route 2, 2 for route 3
        int numberOfStops = 4 + numberOfCustomerStops; // DEPOT + WAREHOUSE + customers + DEPOT
        
        for (int i = 1; i <= numberOfStops; i++) {
            final int stopIndex = i; // Make effectively final for lambda
            RouteStopDao stop = new RouteStopDao();
            stop.setDescription("Stop " + stopIndex + " for route " + routeNumber);
            stop.setPriority(stopIndex);
            stop.setDuration("30 minutes");
            stop.setRoute(route);
            
            // Route structure: DEPOT -> WAREHOUSE -> CUSTOMER(s) -> DEPOT
            if (stopIndex == 1) {
                stop.setStopType(StopType.DEPOT);
            } else if (stopIndex == 2) {
                stop.setStopType(StopType.WAREHOUSE);
            } else if (stopIndex == numberOfStops) {
                stop.setStopType(StopType.DEPOT);
            } else {
                stop.setStopType(StopType.CUSTOMER);
            }
            
            // Create or get location for this stop
            // Use different postal codes to create different locations
            String postalCode = String.format("5%04d", (routeNumber * 10 + stopIndex));
            LocationDao location = locationRepository
                    .findByPostalCode(postalCode)
                    .orElseGet(() -> {
                        LocationDao loc = new LocationDao();
                        loc.setPostalCode(postalCode);
                        loc.setCity("Amsterdam");
                        loc.setAddress("Stop Street " + stopIndex);
                        // Vary coordinates slightly for each stop
                        loc.setLatitude(52.37 + (routeNumber * 0.01) + (stopIndex * 0.005));
                        loc.setLongitude(4.90 + (routeNumber * 0.01) + (stopIndex * 0.005));
                        return locationRepository.save(loc);
                    });
            stop.setLocation(location);
            
            stop = routeStopRepository.save(stop);
            
            // Create a parcel for this stop (only for CUSTOMER stops)
            if (stop.getStopType() == StopType.CUSTOMER) {
                ParcelDao parcel = new ParcelDao();
                parcel.setName("Parcel for Route " + routeNumber + " Stop " + stopIndex);
                parcel.setWarehouse(warehouse);
                parcel.setDeliveryLocation(location);
                parcel.setWeight(5.5 + (stopIndex * 0.5)); // Vary weight slightly
                parcel.setVolume(0.3 + (stopIndex * 0.1)); // Vary volume slightly
                parcel.setStatus(StatusEnum.PLANNED); // Status for assigned parcels
                parcel.setDeliveryInstructions("Leave at the front door");
                parcel.setRecipientName("Customer " + routeNumber + "-" + stopIndex);
                parcel.setRecipientPhone("+3161234567" + stopIndex);
                parcel.setPlannedDeliveryDate(route.getScheduleDate());
                parcel.setStop(stop);
                
                parcelRepository.save(parcel);
            }
            
            stops.add(stop);
        }
        
        return stops;
    }

    /**
     * Create 200 parcels from 10 different warehouses (20 parcels per warehouse)
     */
    private void create200ParcelsFrom10Warehouses() {
        System.out.println("Creating 200 parcels from 10 warehouses...");

        // Define 10 warehouse locations across Netherlands
        String[][] warehouseData = {
            {"Warehouse-Amsterdam", "Amsterdam", "1000AA", "52.3676", "4.9041"},
            {"Warehouse-Rotterdam", "Rotterdam", "3000AA", "51.9244", "4.4777"},
            {"Warehouse-Utrecht", "Utrecht", "3500AA", "52.0907", "5.1214"},
            {"Warehouse-Eindhoven", "Eindhoven", "5600AA", "51.4416", "5.4697"},
            {"Warehouse-Groningen", "Groningen", "9700AA", "53.2194", "6.5665"},
            {"Warehouse-Tilburg", "Tilburg", "5000AA", "51.5555", "5.0913"},
            {"Warehouse-Almere", "Almere", "1300AA", "52.3508", "5.2647"},
            {"Warehouse-Breda", "Breda", "4800AA", "51.5719", "4.7683"},
            {"Warehouse-Nijmegen", "Nijmegen", "6500AA", "51.8426", "5.8518"},
            {"Warehouse-Apeldoorn", "Apeldoorn", "7300AA", "52.2112", "5.9699"}
        };

        // Create 20 parcels for each warehouse
        for (int warehouseIndex = 0; warehouseIndex < warehouseData.length; warehouseIndex++) {
            String[] warehouse = warehouseData[warehouseIndex];
            String warehouseName = warehouse[0];
            String city = warehouse[1];
            String postalCode = warehouse[2];
            double latitude = Double.parseDouble(warehouse[3]);
            double longitude = Double.parseDouble(warehouse[4]);

            // Make effectively final copies for lambda expressions
            final int finalWarehouseIndex = warehouseIndex;
            final String finalCity = city;
            final String finalPostalCode = postalCode;
            final double finalLatitude = latitude;
            final double finalLongitude = longitude;

            // Create warehouse location
            LocationDao warehouseLocation = locationRepository
                    .findByPostalCode(finalPostalCode)
                    .orElseGet(() -> {
                        LocationDao loc = new LocationDao();
                        loc.setPostalCode(finalPostalCode);
                        loc.setCity(finalCity);
                        loc.setAddress("Warehouse Street " + (finalWarehouseIndex + 1));
                        loc.setLatitude(finalLatitude);
                        loc.setLongitude(finalLongitude);
                        return locationRepository.save(loc);
                    });

            // Create warehouse
            WareHouseDao warehouseDao = wareHouseRepository
                    .findByName(warehouseName)
                    .orElseGet(() -> {
                        WareHouseDao w = new WareHouseDao();
                        w.setName(warehouseName);
                        w.setLocation(warehouseLocation);
                        return wareHouseRepository.save(w);
                    });

            // Create 20 parcels for this warehouse
            for (int parcelIndex = 1; parcelIndex <= 20; parcelIndex++) {
                String parcelName = String.format("P%02d-%02d", finalWarehouseIndex + 1, parcelIndex);

                // Create varied delivery locations around Netherlands
                String deliveryPostalCode = String.format("%d%03d", 2000 + finalWarehouseIndex, parcelIndex);

                // Make effectively final copies for lambda expressions
                final int finalParcelIndex = parcelIndex;
                final String finalDeliveryPostalCode = deliveryPostalCode;

                LocationDao deliveryLocation = locationRepository
                        .findByPostalCode(finalDeliveryPostalCode)
                        .orElseGet(() -> {
                            LocationDao loc = new LocationDao();
                            loc.setPostalCode(finalDeliveryPostalCode);
                            loc.setCity("DeliveryCity-" + (finalWarehouseIndex + 1));
                            loc.setAddress("Delivery Street " + finalParcelIndex);
                            // Vary delivery coordinates around warehouse location
                            loc.setLatitude(finalLatitude + ((finalParcelIndex % 10 - 5) * 0.01));
                            loc.setLongitude(finalLongitude + ((finalParcelIndex % 10 - 5) * 0.01));
                            return locationRepository.save(loc);
                        });

                // Create parcel if it doesn't exist
                if (!parcelRepository.existsByName(parcelName)) {
                    ParcelDao parcel = new ParcelDao();
                    parcel.setName(parcelName);
                    parcel.setWarehouse(warehouseDao);
                    parcel.setDeliveryLocation(deliveryLocation);
                    parcel.setWeight(1.0 + (finalParcelIndex % 10)); // Weight 1-10 kg
                    parcel.setVolume(0.1 + (finalParcelIndex % 5) * 0.1); // Volume 0.1-0.5 m³
                    parcel.setStatus(StatusEnum.PENDING);
                    parcel.setDeliveryInstructions("Delivery instructions for " + parcelName);
                    parcel.setRecipientName("Customer-" + finalWarehouseIndex + "-" + finalParcelIndex);
                    parcel.setRecipientPhone("+3161234" + String.format("%04d", finalWarehouseIndex * 100 + finalParcelIndex));
                    parcel.setPlannedDeliveryDate(ZonedDateTime.now().plusDays(1 + (finalParcelIndex % 7))); // Spread over week

                    parcelRepository.save(parcel);
                }
            }

            System.out.println("Created 20 parcels for " + warehouseName + " in " + city);
        }

        System.out.println("✅ Successfully created 200 parcels from 10 warehouses!");
    }
}
