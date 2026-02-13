package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.DriverTruckAssignmentDao;
import com.saxion.proj.tfms.planner.abstractions.driverServices.IUpdateDriverAvailability;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import com.saxion.proj.tfms.planner.repository.DriverTruckAssignmentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("updateDriverAvailabilityHandler")
@Transactional
public class UpdateDriverAvailabilityHandler implements IUpdateDriverAvailability {

    private final DriverRepository driverRepository;
    private final DriverTruckAssignmentRepository assignmentRepository;

    public UpdateDriverAvailabilityHandler(DriverRepository driverRepository,
                                           DriverTruckAssignmentRepository assignmentRepository) {
        this.driverRepository = driverRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public ApiResponse<DriverResponseDto> Handle(Long driverId, boolean isAvailable) {
        if (driverId == null || driverId <= 0)
            return ApiResponse.error("Invalid driver ID");

        DriverDao driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null)
            return ApiResponse.error("Driver not found");

        driver.setIsAvailable(isAvailable);
        driverRepository.save(driver);

        // If availability updated to true, mark all pending assignments as completed
        if (isAvailable) {
            List<DriverTruckAssignmentDao> assignments = assignmentRepository.findAllByDriverAndAssignmentStatus(
                    driver, StatusEnum.COMPLETED);

            assignments.forEach(a -> {
                a.setAssignmentStatus(StatusEnum.COMPLETED);
                assignmentRepository.save(a);
            });
        }

        //Long id, String userName, String email, String city, boolean isAvailable
        DriverResponseDto response = new DriverResponseDto(
                driver.getId(),
                driver.getUser().getUsername(),
                driver.getUser().getEmail(),
                driver.getIsAvailable(),
                driver.getLocation() != null ? driver.getLocation().getCity() : null,
                driver.getLocation() != null ? driver.getLocation().getAddress() : null);

        return ApiResponse.success(response, "Available drivers updated successfully");

    }
}
