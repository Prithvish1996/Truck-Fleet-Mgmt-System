package com.saxion.proj.tfms.truck.controller;

import com.saxion.proj.tfms.truck.dto.TruckDTO;
import com.saxion.proj.tfms.truck.model.Truck;
import com.saxion.proj.tfms.truck.service.TruckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for truck management
 */
@RestController
@RequestMapping("/api/trucks")
@RequiredArgsConstructor
@Slf4j
public class TruckController {
    
    private final TruckService truckService;
    
    /**
     * Create a new truck
     */
    @PostMapping
    public ResponseEntity<TruckDTO> createTruck(@RequestParam String truckId, 
                                          @RequestParam BigDecimal weightLimit) {
        try {
            Truck truck = truckService.createTruck(truckId, weightLimit);
            TruckDTO dto = truckService.convertToDTO(truck);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error creating truck: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all trucks
     */
    @GetMapping
    public ResponseEntity<List<TruckDTO>> getAllTrucks() {
        List<Truck> trucks = truckService.getAllTrucks();
        List<TruckDTO> dtos = truckService.convertToDTOs(trucks);
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Get truck by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Truck> getTruckById(@PathVariable Long id) {
        Optional<Truck> truck = truckService.getTruckById(id);
        return truck.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get truck by truck ID
     */
    @GetMapping("/by-truck-id/{truckId}")
    public ResponseEntity<Truck> getTruckByTruckId(@PathVariable String truckId) {
        Optional<Truck> truck = truckService.getTruckByTruckId(truckId);
        return truck.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all available trucks
     */
    @GetMapping("/available")
    public ResponseEntity<List<Truck>> getAvailableTrucks() {
        List<Truck> trucks = truckService.getAvailableTrucks();
        return ResponseEntity.ok(trucks);
    }
    
    /**
     * Get trucks that can carry specified weight
     */
    @GetMapping("/can-carry")
    public ResponseEntity<List<Truck>> getTrucksThatCanCarry(@RequestParam BigDecimal requiredWeight) {
        List<Truck> trucks = truckService.getTrucksThatCanCarry(requiredWeight);
        return ResponseEntity.ok(trucks);
    }
    
    /**
     * Get trucks by weight limit range
     */
    @GetMapping("/by-weight-range")
    public ResponseEntity<List<Truck>> getTrucksByWeightLimitRange(
            @RequestParam BigDecimal minWeight, 
            @RequestParam BigDecimal maxWeight) {
        List<Truck> trucks = truckService.getTrucksByWeightLimitRange(minWeight, maxWeight);
        return ResponseEntity.ok(trucks);
    }
    
    /**
     * Update truck availability
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<Truck> updateTruckAvailability(@PathVariable Long id, 
                                                       @RequestParam boolean isAvailable) {
        try {
            Truck truck = truckService.updateTruckAvailability(id, isAvailable);
            return ResponseEntity.ok(truck);
        } catch (IllegalArgumentException e) {
            log.error("Error updating truck availability: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update truck weight limit
     */
    @PutMapping("/{id}/weight-limit")
    public ResponseEntity<Truck> updateTruckWeightLimit(@PathVariable Long id, 
                                                     @RequestParam BigDecimal newWeightLimit) {
        try {
            Truck truck = truckService.updateTruckWeightLimit(id, newWeightLimit);
            return ResponseEntity.ok(truck);
        } catch (IllegalArgumentException e) {
            log.error("Error updating truck weight limit: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete truck
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTruck(@PathVariable Long id) {
        try {
            truckService.deleteTruck(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting truck: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Check if truck can carry additional weight
     */
    @GetMapping("/{id}/can-carry")
    public ResponseEntity<Boolean> canTruckCarry(@PathVariable Long id, 
                                              @RequestParam BigDecimal currentLoad, 
                                              @RequestParam BigDecimal additionalWeight) {
        try {
            boolean canCarry = truckService.canTruckCarry(id, currentLoad, additionalWeight);
            return ResponseEntity.ok(canCarry);
        } catch (IllegalArgumentException e) {
            log.error("Error checking truck capacity: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get truck's remaining capacity
     */
    @GetMapping("/{id}/remaining-capacity")
    public ResponseEntity<BigDecimal> getTruckRemainingCapacity(@PathVariable Long id, 
                                                              @RequestParam BigDecimal currentLoad) {
        try {
            BigDecimal remainingCapacity = truckService.getTruckRemainingCapacity(id, currentLoad);
            return ResponseEntity.ok(remainingCapacity);
        } catch (IllegalArgumentException e) {
            log.error("Error getting truck remaining capacity: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
