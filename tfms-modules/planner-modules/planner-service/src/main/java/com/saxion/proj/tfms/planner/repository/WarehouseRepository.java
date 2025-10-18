package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.model.WareHouseDao;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<WareHouseDao, Long>{
    Optional<WareHouseDao> findById(Long id);

    boolean existsByName(String name);
}