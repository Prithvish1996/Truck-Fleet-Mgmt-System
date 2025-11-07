package com.saxion.proj.tfms.routing.repository;

import com.saxion.proj.tfms.commons.model.TruckDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckNeededForRoutingRepository extends JpaRepository<TruckDao, Long> {
    List<TruckDao> findByIsAvailableTrue();

    Optional<TruckDao> findByName(String name);
}
