package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.*;
import com.saxion.proj.tfms.planner.abstractions.driverServices.ICreateDriverAvailability;
import com.saxion.proj.tfms.planner.repository.DriverAvailabilityRepository;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@Qualifier("createDriverAvailability")
public class CreateDriverAvailabilityHandler implements ICreateDriverAvailability {
    private final DriverRepository driverRepository;
    private final DriverAvailabilityRepository availabilityRepository;

    @Autowired
    public CreateDriverAvailabilityHandler(DriverRepository driverRepository,
                                           DriverAvailabilityRepository availabilityRepository) {
        this.driverRepository = driverRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Override
    @Transactional
    public ApiResponse<String> Handle(Long driverId, List<ZonedDateTime> dates) {

        if (driverId == null || driverId <= 0) {
            return ApiResponse.error("Invalid driver ID");
        }

        Optional<DriverDao> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return ApiResponse.error("Driver not found");
        }

        if (dates == null || dates.isEmpty()) {
            return ApiResponse.error("Availability date list cannot be empty");
        }

        DriverDao driver = driverOpt.get();
        ZonedDateTime now = ZonedDateTime.now();

        // Deduplicate dates
        Set<ZonedDateTime> uniqueDates = new HashSet<>(dates);

        for (ZonedDateTime date : uniqueDates) {
            if (date == null) {
                return ApiResponse.error("Date cannot be null");
            }
            if (date.isBefore(now)) {
                return ApiResponse.error("Date must not be in the past: " + date);
            }
        }

        List<DriverAvailabilityDao> availabilities = uniqueDates.stream().map(date -> {
            DriverAvailabilityDao entity = new DriverAvailabilityDao();
            entity.setAvailableAt(date);
            entity.setStatus(StatusEnum.AVAILABLE); // FIXED
            entity.setDriver(driver);
            return entity;
        }).toList();

        availabilityRepository.saveAll(availabilities);

        return ApiResponse.success(availabilities.size() + " availability dates saved successfully");
    }
}