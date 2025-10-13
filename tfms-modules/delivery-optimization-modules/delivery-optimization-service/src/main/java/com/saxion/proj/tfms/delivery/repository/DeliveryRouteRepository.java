package com.saxion.proj.tfms.delivery.repository;

import com.saxion.proj.tfms.delivery.model.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for DeliveryRoute entities
 */
@Repository
public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
    
    /**
     * Find routes by truck ID
     */
    List<DeliveryRoute> findByTruckId(String truckId);
    
    /**
     * Find optimized routes
     */
    List<DeliveryRoute> findByIsOptimizedTrue();
    
    /**
     * Find routes within distance range
     */
    @Query("SELECT r FROM DeliveryRoute r WHERE r.totalDistance BETWEEN :minDistance AND :maxDistance")
    List<DeliveryRoute> findByDistanceRange(@Param("minDistance") BigDecimal minDistance, 
                                          @Param("maxDistance") BigDecimal maxDistance);
    
    /**
     * Find routes by truck
     */
    @Query("SELECT r FROM DeliveryRoute r WHERE r.truckId = :truckId")
    List<DeliveryRoute> findByTruck(@Param("truckId") String truckId);
    
    /**
     * Find routes with package count
     */
    @Query("SELECT r FROM DeliveryRoute r WHERE SIZE(r.packages) = :packageCount")
    List<DeliveryRoute> findByPackageCount(@Param("packageCount") int packageCount);
}
