package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.DepotDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<DepotDao, Long> {
    Optional<DepotDao> findById(Long id);

    Optional<DepotDao> findByName(String name);

    boolean existsByName(String name);
}
