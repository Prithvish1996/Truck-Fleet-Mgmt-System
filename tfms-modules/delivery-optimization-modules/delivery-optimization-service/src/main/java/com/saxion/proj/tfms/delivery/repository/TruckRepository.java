package com.saxion.proj.tfms.delivery.repository;

import com.saxion.proj.tfms.delivery.model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Truck entities
 */
@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {
    
    /**
     * Find truck by truck ID
     */
    Truck findByTruckId(String truckId);
    
    /**
     * Find available trucks
     */
    List<Truck> findByIsAvailableTrue();
    
    /**
     * Find trucks by weight limit range
     */
    @Query("SELECT t FROM Truck t WHERE t.weightLimit BETWEEN :minWeight AND :maxWeight")
    List<Truck> findByWeightLimitRange(@Param("minWeight") BigDecimal minWeight, 
                                      @Param("maxWeight") BigDecimal maxWeight);
    
    /**
     * Find trucks that can carry specified weight
     */
    @Query("SELECT t FROM Truck t WHERE t.weightLimit >= :requiredWeight AND t.isAvailable = true")
    List<Truck> findTrucksThatCanCarry(@Param("requiredWeight") BigDecimal requiredWeight);
    
    /**
     * Find trucks with remaining capacity
     */
    @Query("SELECT t FROM Truck t WHERE t.isAvailable = true")
    List<Truck> findAvailableTrucks();
}
