package com.saxion.proj.tfms.driver.controller;

import com.saxion.proj.tfms.driver.model.Driver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    // Simple in-memory storage for demo purposes
    private final Map<Long, Driver> drivers = new HashMap<>();
    private Long nextId = 1L;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "driver-service");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(new ArrayList<>(drivers.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        Driver driver = drivers.get(id);
        return driver != null ? ResponseEntity.ok(driver) : ResponseEntity.notFound().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers() {
        List<Driver> availableDrivers = drivers.values().stream()
                .filter(driver -> driver.getStatus() == Driver.DriverStatus.AVAILABLE)
                .toList();
        return ResponseEntity.ok(availableDrivers);
    }

    @PostMapping
    public ResponseEntity<Driver> createDriver(@RequestBody Driver driver) {
        driver.setId(nextId++);
        drivers.put(driver.getId(), driver);
        return ResponseEntity.ok(driver);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Driver> updateDriver(@PathVariable Long id, @RequestBody Driver driverDetails) {
        Driver driver = drivers.get(id);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        driver.setName(driverDetails.getName());
        driver.setEmail(driverDetails.getEmail());
        driver.setPhone(driverDetails.getPhone());
        driver.setLicenseNumber(driverDetails.getLicenseNumber());
        driver.setStatus(driverDetails.getStatus());
        driver.preUpdate();

        return ResponseEntity.ok(driver);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Driver> updateDriverStatus(@PathVariable Long id, @RequestParam Driver.DriverStatus status) {
        Driver driver = drivers.get(id);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        driver.setStatus(status);
        driver.preUpdate();
        return ResponseEntity.ok(driver);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        if (drivers.remove(id) != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
