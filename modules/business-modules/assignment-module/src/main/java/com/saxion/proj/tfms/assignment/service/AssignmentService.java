package com.saxion.proj.tfms.assignment.service;

import com.saxion.proj.tfms.assignment.model.Assignment;
import com.saxion.proj.tfms.assignment.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public Optional<Assignment> getAssignmentById(Long id) {
        return assignmentRepository.findById(id);
    }

    public Assignment createAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public Assignment updateAssignment(Long id, Assignment assignmentDetails) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));

        assignment.setDriverId(assignmentDetails.getDriverId());
        assignment.setTruckId(assignmentDetails.getTruckId());
        assignment.setOrderId(assignmentDetails.getOrderId());
        assignment.setStatus(assignmentDetails.getStatus());
        assignment.setNotes(assignmentDetails.getNotes());

        return assignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

    public List<Assignment> getAssignmentsByDriver(Long driverId) {
        return assignmentRepository.findByDriverId(driverId);
    }

    public List<Assignment> getAssignmentsByTruck(Long truckId) {
        return assignmentRepository.findByTruckId(truckId);
    }

    public List<Assignment> getAssignmentsByStatus(Assignment.AssignmentStatus status) {
        return assignmentRepository.findByStatus(status);
    }

    public Assignment startAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));
        
        assignment.setStatus(Assignment.AssignmentStatus.IN_PROGRESS);
        assignment.setStartedAt(LocalDateTime.now());
        
        return assignmentRepository.save(assignment);
    }

    public Assignment completeAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));
        
        assignment.setStatus(Assignment.AssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());
        
        return assignmentRepository.save(assignment);
    }
}
