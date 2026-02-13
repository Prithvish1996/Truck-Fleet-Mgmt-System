package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationDao, Long> {
    Optional<LocationDao> findById(Long id);

    Optional<LocationDao> findByPostalCode(String postalcode);

    boolean existsByPostalCode(String postalcode);

    /**
     * Find a location by exact latitude and longitude.
     * Returns an Optional — empty if no match.
     */
    @Query("SELECT l FROM LocationDao l WHERE l.latitude = :latitude AND l.longitude = :longitude")
    Optional<LocationDao> findByLatitudeAndLongitude(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude
    );

    @Query("SELECT l FROM LocationDao l WHERE l.latitude = :latitude AND l.longitude = :longitude")
    List<LocationDao> findByLatAndLong(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude
    );
}

