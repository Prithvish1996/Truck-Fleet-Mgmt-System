package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.TruckDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<TruckDao, Long> {

    /**
     * Find all active trucks that are available for assignment.
     * Assumes a truck has a field 'isAvailable' in the TruckDao entity.
     */
    List<TruckDao> findAllByIsAvailableTrue();

    /**
     * Find a truck by its license plate number (unique identifier).
     */
    Optional<TruckDao> findByPlateNumber(String licensePlate);

    /**
     * Count how many trucks are currently available for assignment.
     */
    @Query("SELECT COUNT(t) FROM TruckDao t WHERE t.isAvailable = true")
    long countAvailableTrucks();

    /**
     * Check if a specific truck is available for assignment.
     */
    @Query("""
        SELECT CASE WHEN t.isAvailable = true THEN true ELSE false END
        FROM TruckDao t WHERE t.id = :truckId
    """)
    boolean isTruckAvailable(@Param("truckId") Long truckId);

    /**
     * Retrieve all trucks that are NOT currently available (e.g., already assigned or under maintenance).
     */
    List<TruckDao> findAllByIsAvailableFalse();

    /**
     * Find trucks by their make or type (for filtering UI or reports).
     */
    @Query("SELECT t FROM TruckDao t WHERE LOWER(t.make) LIKE LOWER(CONCAT('%', :model, '%'))")
    List<TruckDao> searchByModel(@Param("model") String model);

    /**
     * Checks if a truck with the given plate number already exists.
     *
     * @param plateNumber the truck's plate number
     * @return true if a truck with the given plate number exists, false otherwise
     */
    boolean existsByPlateNumber(String plateNumber);
}
