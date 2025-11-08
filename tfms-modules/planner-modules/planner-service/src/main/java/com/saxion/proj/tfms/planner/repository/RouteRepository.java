package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.RouteDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<RouteDao, Long> {
    List<RouteDao> findByStatus(StatusEnum status);

    /**
     * Retrieves all routes for a given driver and status.
     *
     * @param driverId The driver ID.
     * @param status   The route status (e.g., ASSIGNED, PLANNED).
     * @return List of matching RouteDao entities.
     */
    List<RouteDao> findAllByDriverIdAndStatus(Long driverId, StatusEnum status);

    /**
     * Retrieves all routes for a given driver and status.
     *
     * @param truckId The driver ID.
     * @param status   The route status (e.g., ASSIGNED, PLANNED).
     * @return List of matching RouteDao entities.
     */
    List<RouteDao> findAllByTruckIdAndStatus(Long truckId, StatusEnum status);
}
