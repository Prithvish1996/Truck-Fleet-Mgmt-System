package com.saxion.proj.tfms.config;

import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.repository.DepotRepository;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.saxion.proj.tfms.commons.model.UserDao;
import com.saxion.proj.tfms.commons.model.UserDao.UserType;
import com.saxion.proj.tfms.auth.repository.AuthUserRepository;

import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;

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
        
        // Create default planner user
        createUserIfNotExists("planner@tfms.com", "planner", "planner123", UserType.PLANNER);
        
        // Create default test user
        createUserIfNotExists("test@example.com", "testuser", "password123", UserType.DRIVER);

        // Create default warehouse
        createDepotIfNotExists("Main Depot", 500.0);

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
}
