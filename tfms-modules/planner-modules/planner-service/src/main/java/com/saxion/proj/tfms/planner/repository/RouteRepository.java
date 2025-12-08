package com.saxion.proj.tfms.planner.repository;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.RouteDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<RouteDao, Long> {
    List<RouteDao> findByStatus(StatusEnum status);

    List<RouteDao> findAllByDriverIdAndStatus(Long driverId, StatusEnum status);

    List<RouteDao> findAllByDriverIdAndStatusIn(Long driverId, List<StatusEnum> statuses);

    List<RouteDao> findAllByTruckIdAndStatus(Long truckId, StatusEnum status);
}
