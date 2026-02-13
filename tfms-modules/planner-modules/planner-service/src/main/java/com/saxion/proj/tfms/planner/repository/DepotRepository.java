package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<DepotDao, Long> {
    Optional<DepotDao> findById(Long id);

    Optional<DepotDao> findByName(String name);

    boolean existsByName(String name);

    /**
     * Fetch all active depot (used in listing).
     */
    Page<DepotDao> findByIsActiveTrue(Pageable pageable);
}
