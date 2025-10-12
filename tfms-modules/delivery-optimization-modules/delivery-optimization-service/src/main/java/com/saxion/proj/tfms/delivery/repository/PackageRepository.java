package com.saxion.proj.tfms.delivery.repository;

import com.saxion.proj.tfms.delivery.model.DeliveryPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Package entities
 */
@Repository
public interface PackageRepository extends JpaRepository<DeliveryPackage, Long> {
    
    /**
     * Find packages by warehouse ID
     */
    List<DeliveryPackage> findByWarehouseId(Long warehouseId);
    
    /**
     * Find packages by delivery route ID
     */
    List<DeliveryPackage> findByDeliveryRouteId(Long deliveryRouteId);
    
    /**
     * Find packages not assigned to any route
     */
    @Query("SELECT p FROM DeliveryPackage p WHERE p.deliveryRoute IS NULL")
    List<DeliveryPackage> findUnassignedPackages();
    
    /**
     * Find packages by delivery date
     */
    List<DeliveryPackage> findByDeliveryDate(String deliveryDate);
    
    /**
     * Find packages within weight range
     */
    @Query("SELECT p FROM DeliveryPackage p WHERE p.weight BETWEEN :minWeight AND :maxWeight")
    List<DeliveryPackage> findByWeightRange(@Param("minWeight") Double minWeight, 
                                   @Param("maxWeight") Double maxWeight);
}
