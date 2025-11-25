package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.DriverAvailabilityDao;
import com.saxion.proj.tfms.commons.model.DriverSuggestionDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface DriverSuggestionRepository extends JpaRepository<DriverSuggestionDao, Long> {
    Optional<DriverSuggestionDao> findById(Long id);

    // Check duplicate suggestion per driver
    boolean existsByDriver_IdAndSuggestion(Long driverId, String suggestion);
}
