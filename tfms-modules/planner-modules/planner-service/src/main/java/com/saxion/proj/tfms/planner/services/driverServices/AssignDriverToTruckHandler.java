package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.DriverTruckAssignmentDao;
import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.planner.abstractions.driverServices.IAssignDriverToTruck;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import com.saxion.proj.tfms.planner.repository.DriverTruckAssignmentRepository;
import com.saxion.proj.tfms.planner.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@Qualifier("assignDriverToTruckHandler")
@Transactional
public class AssignDriverToTruckHandler implements IAssignDriverToTruck {

    private final DriverRepository driverRepository;
    private final TruckRepository truckRepository;
    private final DriverTruckAssignmentRepository assignmentRepository;

    public AssignDriverToTruckHandler(DriverRepository driverRepository,
                                      TruckRepository truckRepository,
                                      DriverTruckAssignmentRepository assignmentRepository){
        this.driverRepository = driverRepository;
        this.truckRepository = truckRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public ApiResponse<String> Handle(Long driverId, Long truckId) {
        if (driverId == null || driverId <= 0 || truckId == null || truckId <= 0)
            return ApiResponse.error("Invalid driver or truck ID");

        Optional<DriverDao> driverOpt = driverRepository.findById(driverId);
        Optional<TruckDao> truckOpt = truckRepository.findById(truckId);

        if (driverOpt.isEmpty())
            return ApiResponse.error("Driver not found");
        if (truckOpt.isEmpty())
            return ApiResponse.error("Truck not found");

        DriverDao driver = driverOpt.get();
        TruckDao truck = truckOpt.get();

        // Business Rule 1: Check if driver already has assignment today
        boolean alreadyAssigned = assignmentRepository.existsByDriverAndDateAssignedBetween(
                driver,
                ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().getZone()),
                ZonedDateTime.now().toLocalDate().plusDays(1).atStartOfDay(ZonedDateTime.now().getZone())
        );

        if (alreadyAssigned)
            return ApiResponse.error("Driver already has a truck assignment for today");

        // Create new assignment
        DriverTruckAssignmentDao assignment = new DriverTruckAssignmentDao();
        assignment.setDriver(driver);
        assignment.setTruck(truck);
        assignment.setDateAssigned(ZonedDateTime.now());
        assignment.setAssignmentStatus(StatusEnum.ASSIGNED);

        assignmentRepository.save(assignment);

        // Business Rule 2: Send notification
        sendDriverAssignmentNotification(driver, truck);

        return ApiResponse.success("Truck assigned successfully and notification sent");
    }

    private void sendDriverAssignmentNotification(DriverDao driver, TruckDao truck) {
        // Simplified — integrate with real email service later
        String email = driver.getUser().getEmail();
        String subject = "Truck Assignment Notification";
        String message = String.format("Dear %s, you have been assigned to truck %s today.",
                driver.getUser().getUsername(), truck.getPlateNumber());
        System.out.printf("Email sent to %s: %s - %s%n", email, subject, message);
    }
}
