package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.UserDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import java.util.Optional;
import java.util.List;

@Repository
public interface ParcelRepository extends JpaRepository<ParcelDao, Long> {
    Optional<ParcelDao> findById(Long id);

    boolean existsByName(String name);

    @Query("SELECT p FROM ParcelDao p WHERE p.warehouse.id = :id AND p.active = true")
    Optional<ParcelDao> findActiveByWarehouse(@Param("id") Long id);

    @Query("SELECT p.id FROM ParcelDao p ORDER BY p.id ASC")
    List<Long> findParcelIds(Pageable pageable);

    // Step 2: fetch full parcel data with all needed joins
    @Query("""
        SELECT DISTINCT p FROM ParcelDao p
        LEFT JOIN FETCH p.deliveryLocation dl
        LEFT JOIN FETCH p.warehouse w
        LEFT JOIN FETCH w.location wl
        WHERE p.id IN :ids
        """)
    List<ParcelDao> findAllWithLocations(@Param("ids") List<Long> ids);

    @Query("""
        SELECT p FROM ParcelDao p
        LEFT JOIN FETCH p.deliveryLocation dl
        LEFT JOIN FETCH p.warehouse w
        LEFT JOIN FETCH w.location wl
        WHERE p.id = :id
        """)
    Optional<ParcelDao> findByIdWithRelations(@Param("id") Long id);

    @Query("""
        SELECT p FROM ParcelDao p
        LEFT JOIN FETCH p.deliveryLocation dl
        LEFT JOIN FETCH p.warehouse w
        LEFT JOIN FETCH w.location wl
        """)
    Optional<ParcelDao> findAllWithRelations();

    // Fetch all parcels by status and planned delivery date.
    @Query("SELECT p FROM ParcelDao p " +
            "WHERE p.status = :status " +
            "AND DATE(p.plannedDeliveryDate) = :plannedDate")
    Page<ParcelDao> findByStatusAndPlannedDeliveryDate(
            @Param("status") StatusEnum status,
            @Param("plannedDate") LocalDate plannedDate,
            Pageable pageable
    );

    // Fetch all parcels by status and planned delivery date. No pagination.
    @Query("SELECT p FROM ParcelDao p " +
            "WHERE p.status = :status " +
            "AND DATE(p.plannedDeliveryDate) = :plannedDate")
    List<ParcelDao> findAllByStatusAndPlannedDeliveryDate(
            @Param("status") StatusEnum status,
            @Param("plannedDate") LocalDate plannedDate
    );

    // Counts parcels by warehouse and status
    long countByWarehouseAndStatus(WareHouseDao warehouse, StatusEnum status);

}