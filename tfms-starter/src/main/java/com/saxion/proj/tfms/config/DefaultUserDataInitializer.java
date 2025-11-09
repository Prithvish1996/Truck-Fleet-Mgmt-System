package com.saxion.proj.tfms.config;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
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

        // Create default parcels
        createDefaultWarehouseAndParcel("Parcel A", "Deventer","Central Warehouse","22222");
        createDefaultWarehouseAndParcel("Parcel B", "Deventer","Central Warehouse","22222");
        createDefaultWarehouseAndParcel("Parcel C", "Deventer","Central Warehouse","22222");
        createDefaultWarehouseAndParcel("Parcel D", "Deventer","Central Warehouse","22222");
        createDefaultWarehouseAndParcel("Parcel E", "Deventer","Central Warehouse","22222");

        // Create default drivers
        createDefaultDriver("driver@tfms.com", "Deventer", "22222");
        createDefaultDriver("driver2@tfms.com", "Utrecht", "44444");
        createDefaultDriver("driver3@tfms.com", "Arnhem", "66666");

        // Create default trucks
        createDefaultTruck("TRK-001", TruckType.SMALL, 20000.0);
        createDefaultTruck("TRK-002", TruckType.SMALL, 20000.0);
        createDefaultTruck("TRK-003", TruckType.MEDIUM, 50000.0);
        createDefaultTruck("TRK-004", TruckType.MEDIUM, 50000.0);
        createDefaultTruck("TRK-005", TruckType.LARGE, 100000.0);

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
}
