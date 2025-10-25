package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.LocationDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<LocationDao, Long> {
}

