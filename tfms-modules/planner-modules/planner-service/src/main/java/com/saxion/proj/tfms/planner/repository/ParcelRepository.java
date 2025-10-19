package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.Optional;
import java.util.List;

@Repository
public interface ParcelRepository extends JpaRepository<ParcelDao, Long> {
    Optional<ParcelDao> findById(Long id);

    boolean existsByName(String name);


    @Query("SELECT p FROM ParcelDao p WHERE p.warehouse.id = :id AND p.active = true")
    Optional<ParcelDao> findActiveByWarehouse(@Param("id") Long id);
}