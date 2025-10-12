package com.saxion.proj.tfms.delivery.repository;

import com.saxion.proj.tfms.delivery.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Warehouse entities
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    /**
     * Find warehouse by name
     */
    Warehouse findByName(String name);
    
    /**
     * Find warehouses by delivery date
     */
    List<Warehouse> findByDeliveryDate(String deliveryDate);
    
    /**
     * Find warehouses with packages
     */
    @Query("SELECT w FROM Warehouse w WHERE SIZE(w.packages) > 0")
    List<Warehouse> findWarehousesWithPackages();
    
    /**
     * Find warehouses with packages within weight range
     */
    @Query("SELECT DISTINCT w FROM Warehouse w JOIN w.packages p WHERE p.weight BETWEEN :minWeight AND :maxWeight")
    List<Warehouse> findWarehousesWithPackagesInWeightRange(@Param("minWeight") Double minWeight, 
                                                          @Param("maxWeight") Double maxWeight);
}
