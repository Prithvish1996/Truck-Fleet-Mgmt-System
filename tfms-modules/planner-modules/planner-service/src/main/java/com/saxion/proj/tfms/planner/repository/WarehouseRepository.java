package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.WareHouseDao;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<WareHouseDao, Long>{
    Optional<WareHouseDao> findById(Long id);

    Optional<WareHouseDao> findByName(String name);

    boolean existsByName(String name);

    /**
     * Fetch all active warehouses (used in listing).
     */
    Page<WareHouseDao> findByActiveTrue(Pageable pageable);

    /**
     * Fetch warehouse details by parcel ID.
     * Joins the Parcel table and retrieves the warehouse linked to that parcel.
     */
    @Query("SELECT p.warehouse FROM ParcelDao p WHERE p.id = :parcelId")
    Optional<WareHouseDao> findWarehouseByParcelId(Long parcelId);
}