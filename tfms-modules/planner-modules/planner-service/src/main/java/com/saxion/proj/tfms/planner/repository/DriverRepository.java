package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.DriverTruckAssignmentDao;
import com.saxion.proj.tfms.commons.model.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<DriverDao, Long> {

    /**
     * Find all available drivers (where isAvailable = true).
     */
    List<DriverDao> findByIsAvailableTrue();

    /**
     * Find driver by user ID (useful for linking user accounts).
     */
    Optional<DriverDao> findByUserId(Long userId);

    /**
     * Checks whether a driver record exists for the given user.
     *
     * @param user the user associated with the driver
     * @return true if a driver exists for the user, false otherwise
     */
    boolean existsByUser(UserDao user);



    /**
     * Check if a driver already has an assignment for a given date.
     * Business Rule: a driver cannot have multiple truck assignments on the same day.
     */
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM DriverTruckAssignmentDao a
        WHERE a.driver.id = :driverId
        AND FUNCTION('DATE', a.dateAssigned) = FUNCTION('DATE', :date)
    """)
    boolean existsActiveAssignmentForDate(@Param("driverId") Long driverId, @Param("date") ZonedDateTime date);

    /**
     * Find all assignments of a given driver.
     */
    @Query("SELECT a FROM DriverTruckAssignmentDao a WHERE a.driver.id = :driverId")
    List<DriverTruckAssignmentDao> findAssignmentsByDriverId(@Param("driverId") Long driverId);

    /**
     * Count how many drivers are currently available.
     */
    @Query("SELECT COUNT(d) FROM DriverDao d WHERE d.isAvailable = true")
    long countAvailableDrivers();
}