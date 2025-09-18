package com.saxion.proj.tfms.assignment.repository;

import com.saxion.proj.tfms.assignment.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    List<Assignment> findByDriverId(Long driverId);
    
    List<Assignment> findByTruckId(Long truckId);
    
    List<Assignment> findByOrderId(Long orderId);
    
    List<Assignment> findByStatus(Assignment.AssignmentStatus status);
    
    @Query("SELECT a FROM Assignment a WHERE a.driverId = :driverId AND a.status = :status")
    List<Assignment> findByDriverIdAndStatus(@Param("driverId") Long driverId, 
                                           @Param("status") Assignment.AssignmentStatus status);
}
