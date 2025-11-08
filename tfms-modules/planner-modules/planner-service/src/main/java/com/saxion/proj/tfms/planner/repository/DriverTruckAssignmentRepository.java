package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.DriverTruckAssignmentDao;
import com.saxion.proj.tfms.commons.model.TruckDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverTruckAssignmentRepository extends JpaRepository<DriverTruckAssignmentDao, Long> {

    /**
     * Check if a driver already has an assignment for the same day.
     * Business Rule 1: A driver can only have one truck assigned per day.
     */
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM DriverTruckAssignmentDao a
        WHERE a.driver.id = :driverId
        AND FUNCTION('DATE', a.dateAssigned) = FUNCTION('DATE', :date)
        AND a.assignmentStatus <> com.saxion.proj.tfms.commons.constants.StatusEnum.COMPLETED
    """)
    boolean existsByDriverAndDate(@Param("driverId") Long driverId, @Param("date") ZonedDateTime date);

    /**
     * Find all active (non-completed) assignments for a given driver.
     */
    List<DriverTruckAssignmentDao> findAllByDriverAndAssignmentStatusNot(DriverDao driver, StatusEnum status);

    /**
     * Find all completed assignments for a given driver.
     */
    List<DriverTruckAssignmentDao> findAllByDriverAndAssignmentStatus(DriverDao driver, StatusEnum status);

    /**
     * Find all assignments for a specific truck.
     */
    List<DriverTruckAssignmentDao> findAllByTruck(TruckDao truck);

    /**
     * Find a specific active assignment by driver and truck.
     */
    Optional<DriverTruckAssignmentDao> findByDriverAndTruckAndAssignmentStatus(
            DriverDao driver,
            TruckDao truck,
            StatusEnum status
    );

    /**
     * Find all assignments created on a given date (useful for reports or scheduling views).
     */
    @Query("""
        SELECT a FROM DriverTruckAssignmentDao a
        WHERE FUNCTION('DATE', a.dateAssigned) = FUNCTION('DATE', :date)
    """)
    List<DriverTruckAssignmentDao> findAllByAssignmentDate(@Param("date") ZonedDateTime date);

    /**
     * Check if a driver already has an assignment between two timestamps (used for "same day" check).
     */
    boolean existsByDriverAndDateAssignedBetween(
            DriverDao driver,
            ZonedDateTime startOfDay,
            ZonedDateTime endOfDay
    );
}
//existsByDriverAndDateAssignedBetween