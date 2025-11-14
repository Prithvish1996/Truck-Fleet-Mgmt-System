package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.DriverAvailabilityDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverAvailabilityRepository  extends JpaRepository<DriverAvailabilityDao, Long>{
    Optional<DriverAvailabilityDao> findById(Long id);
}
