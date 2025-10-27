package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationDao, Long> {
    Optional<LocationDao> findById(Long id);

    Optional<LocationDao> findByPostalCode(String postalcode);

    boolean existsByPostalCode(String postalcode);
}

