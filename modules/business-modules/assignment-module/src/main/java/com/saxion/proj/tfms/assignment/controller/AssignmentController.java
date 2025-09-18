package com.saxion.proj.tfms.assignment.controller;

import com.saxion.proj.tfms.assignment.model.Assignment;
import com.saxion.proj.tfms.assignment.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping
    public List<Assignment> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        return assignmentService.getAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Assignment createAssignment(@RequestBody Assignment assignment) {
        return assignmentService.createAssignment(assignment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable Long id, @RequestBody Assignment assignmentDetails) {
        try {
            Assignment updatedAssignment = assignmentService.updateAssignment(id, assignmentDetails);
            return ResponseEntity.ok(updatedAssignment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/driver/{driverId}")
    public List<Assignment> getAssignmentsByDriver(@PathVariable Long driverId) {
        return assignmentService.getAssignmentsByDriver(driverId);
    }

    @GetMapping("/truck/{truckId}")
    public List<Assignment> getAssignmentsByTruck(@PathVariable Long truckId) {
        return assignmentService.getAssignmentsByTruck(truckId);
    }

    @GetMapping("/status/{status}")
    public List<Assignment> getAssignmentsByStatus(@PathVariable Assignment.AssignmentStatus status) {
        return assignmentService.getAssignmentsByStatus(status);
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<Assignment> startAssignment(@PathVariable Long id) {
        try {
            Assignment assignment = assignmentService.startAssignment(id);
            return ResponseEntity.ok(assignment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Assignment> completeAssignment(@PathVariable Long id) {
        try {
            Assignment assignment = assignmentService.completeAssignment(id);
            return ResponseEntity.ok(assignment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
