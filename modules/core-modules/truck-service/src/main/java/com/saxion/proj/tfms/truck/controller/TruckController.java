package com.saxion.proj.tfms.truck.controller;

import com.saxion.proj.tfms.truck.model.Truck;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/trucks")
public class TruckController {

    private Map<Long, Truck> trucks = new HashMap<>();
    private AtomicLong idCounter = new AtomicLong(1);

    public TruckController() {
        // Initialize with sample data
        Truck truck1 = new Truck("ABC-123", "Volvo", "FH16");
        truck1.setId(idCounter.getAndIncrement());
        truck1.setYear(2022);
        truck1.setCapacityKg(25000.0);
        truck1.setCurrentLocation("Warehouse A");
        trucks.put(truck1.getId(), truck1);

        Truck truck2 = new Truck("XYZ-789", "Mercedes", "Actros");
        truck2.setId(idCounter.getAndIncrement());
        truck2.setYear(2021);
        truck2.setCapacityKg(30000.0);
        truck2.setCurrentLocation("Warehouse B");
        trucks.put(truck2.getId(), truck2);
    }

    @GetMapping
    public List<Truck> getAllTrucks() {
        return new ArrayList<>(trucks.values());
    }

    @GetMapping("/{id}")
    public Truck getTruckById(@PathVariable Long id) {
        return trucks.get(id);
    }

    @PostMapping
    public Truck createTruck(@RequestBody Truck truck) {
        truck.setId(idCounter.getAndIncrement());
        trucks.put(truck.getId(), truck);
        return truck;
    }

    @PutMapping("/{id}")
    public Truck updateTruck(@PathVariable Long id, @RequestBody Truck truckDetails) {
        Truck truck = trucks.get(id);
        if (truck != null) {
            truck.setLicensePlate(truckDetails.getLicensePlate());
            truck.setMake(truckDetails.getMake());
            truck.setModel(truckDetails.getModel());
            truck.setYear(truckDetails.getYear());
            truck.setCapacityKg(truckDetails.getCapacityKg());
            truck.setStatus(truckDetails.getStatus());
            truck.setCurrentLocation(truckDetails.getCurrentLocation());
            truck.updateTimestamp();
        }
        return truck;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteTruck(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        if (trucks.remove(id) != null) {
            response.put("message", "Truck deleted successfully");
        } else {
            response.put("message", "Truck not found");
        }
        return response;
    }

    @GetMapping("/status/{status}")
    public List<Truck> getTrucksByStatus(@PathVariable Truck.TruckStatus status) {
        return trucks.values().stream()
                .filter(truck -> truck.getStatus() == status)
                .toList();
    }

    @PutMapping("/{id}/maintenance")
    public Truck scheduleMaintenance(@PathVariable Long id, @RequestBody Map<String, String> maintenanceData) {
        Truck truck = trucks.get(id);
        if (truck != null) {
            truck.setStatus(Truck.TruckStatus.MAINTENANCE);
            truck.setLastMaintenance(LocalDateTime.now());
            if (maintenanceData.containsKey("nextMaintenance")) {
                truck.setNextMaintenance(LocalDateTime.parse(maintenanceData.get("nextMaintenance")));
            }
            truck.updateTimestamp();
        }
        return truck;
    }
}
