package com.saxion.proj.tfms.truck.service;

import com.saxion.proj.tfms.truck.dto.TruckDTO;
import com.saxion.proj.tfms.truck.model.Truck;
import com.saxion.proj.tfms.truck.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing truck operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TruckService {
    
    private final TruckRepository truckRepository;
    
    /**
     * Create a new truck
     */
    public Truck createTruck(String truckId, BigDecimal weightLimit) {
        log.info("Creating new truck with ID: {} and weight limit: {}", truckId, weightLimit);
        
        if (truckRepository.findByTruckId(truckId) != null) {
            throw new IllegalArgumentException("Truck with ID " + truckId + " already exists");
        }
        
        Truck truck = new Truck(truckId, weightLimit);
        return truckRepository.save(truck);
    }
    
    /**
     * Get all trucks
     */
    @Transactional(readOnly = true)
    public List<Truck> getAllTrucks() {
        log.info("Retrieving all trucks");
        return truckRepository.findAll();
    }
    
    /**
     * Get truck by ID
     */
    @Transactional(readOnly = true)
    public Optional<Truck> getTruckById(Long id) {
        log.info("Retrieving truck by ID: {}", id);
        return truckRepository.findById(id);
    }
    
    /**
     * Get truck by truck ID
     */
    @Transactional(readOnly = true)
    public Optional<Truck> getTruckByTruckId(String truckId) {
        log.info("Retrieving truck by truck ID: {}", truckId);
        return Optional.ofNullable(truckRepository.findByTruckId(truckId));
    }
    
    /**
     * Get all available trucks
     */
    @Transactional(readOnly = true)
    public List<Truck> getAvailableTrucks() {
        log.info("Retrieving all available trucks");
        return truckRepository.findByIsAvailableTrue();
    }
    
    /**
     * Get trucks that can carry specified weight
     */
    @Transactional(readOnly = true)
    public List<Truck> getTrucksThatCanCarry(BigDecimal requiredWeight) {
        log.info("Retrieving trucks that can carry weight: {}", requiredWeight);
        return truckRepository.findTrucksThatCanCarry(requiredWeight);
    }
    
    /**
     * Get trucks by weight limit range
     */
    @Transactional(readOnly = true)
    public List<Truck> getTrucksByWeightLimitRange(BigDecimal minWeight, BigDecimal maxWeight) {
        log.info("Retrieving trucks with weight limit between {} and {}", minWeight, maxWeight);
        return truckRepository.findByWeightLimitRange(minWeight, maxWeight);
    }
    
    /**
     * Update truck availability
     */
    public Truck updateTruckAvailability(Long id, boolean isAvailable) {
        log.info("Updating truck {} availability to: {}", id, isAvailable);
        
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found with ID: " + id));
        
        truck.setIsAvailable(isAvailable);
        return truckRepository.save(truck);
    }
    
    /**
     * Update truck weight limit
     */
    public Truck updateTruckWeightLimit(Long id, BigDecimal newWeightLimit) {
        log.info("Updating truck {} weight limit to: {}", id, newWeightLimit);
        
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found with ID: " + id));
        
        truck.setWeightLimit(newWeightLimit);
        return truckRepository.save(truck);
    }
    
    /**
     * Delete truck
     */
    public void deleteTruck(Long id) {
        log.info("Deleting truck with ID: {}", id);
        
        if (!truckRepository.existsById(id)) {
            throw new IllegalArgumentException("Truck not found with ID: " + id);
        }
        
        truckRepository.deleteById(id);
    }
    
    /**
     * Check if truck can carry additional weight
     */
    @Transactional(readOnly = true)
    public boolean canTruckCarry(Long truckId, BigDecimal currentLoad, BigDecimal additionalWeight) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found with ID: " + truckId));
        
        return truck.canCarry(currentLoad, additionalWeight);
    }
    
    /**
     * Get truck's remaining capacity
     */
    @Transactional(readOnly = true)
    public BigDecimal getTruckRemainingCapacity(Long truckId, BigDecimal currentLoad) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found with ID: " + truckId));
        
        return truck.getRemainingCapacity(currentLoad);
    }
    
    /**
     * Convert Truck entity to DTO
     */
    public TruckDTO convertToDTO(Truck truck) {
        if (truck == null) {
            return null;
        }
        
        TruckDTO dto = new TruckDTO();
        dto.setId(truck.getId());
        dto.setTruckId(truck.getTruckId());
        dto.setWeightLimit(truck.getWeightLimit());
        dto.setIsAvailable(truck.getIsAvailable());
        
        return dto;
    }
    
    /**
     * Convert list of Truck entities to DTOs
     */
    public List<TruckDTO> convertToDTOs(List<Truck> trucks) {
        return trucks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
