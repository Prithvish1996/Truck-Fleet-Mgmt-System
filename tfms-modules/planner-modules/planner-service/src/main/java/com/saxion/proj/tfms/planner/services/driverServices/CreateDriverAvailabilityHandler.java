package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.*;
import com.saxion.proj.tfms.planner.abstractions.driverServices.ICreateDriverAvailability;
import com.saxion.proj.tfms.planner.dto.DriverAvailabilityRequestDto;
import com.saxion.proj.tfms.planner.repository.DriverAvailabilityRepository;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public ApiResponse<String> Handle(Long driverId, List<DriverAvailabilityRequestDto> dates) {

        // 1. Validate driver ID
        if (driverId == null || driverId <= 0) {
            return ApiResponse.error("Invalid driver ID");
        }

        // 2. Ensure driver exists
        DriverDao driver = driverRepository.findById(driverId)
                .orElse(null);

        if (driver == null) {
            return ApiResponse.error("Driver not found");
        }

        // 3. Validate list
        if (dates == null || dates.isEmpty()) {
            return ApiResponse.error("Availability date list cannot be empty");
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZoneId zone = ZoneId.systemDefault();

        // 4. Deduplicate check by LocalDate
        List<DriverAvailabilityRequestDto> uniqueList = duplicateCheck(dates);

        // 5. Validate each DTO
        for (DriverAvailabilityRequestDto dto : uniqueList) {

            if (dto.getAvailableAt() == null) {
                return ApiResponse.error("Date cannot be null");
            }

            // Convert LocalDate → ZonedDateTime
            ZonedDateTime zdt = dto.getAvailableAt().atStartOfDay(zone);

            if (zdt.isBefore(now)) {
                return ApiResponse.error("Date must not be in the past: " + dto.getAvailableAt());
            }
        }

        // 6. Create entity list
        List<DriverAvailabilityDao> availabilities = CreateEntityList(uniqueList, driver, zone);

        // 7. Save all
        availabilityRepository.saveAll(availabilities);

        return ApiResponse.success(availabilities.size() + " availability dates saved successfully");
    }


    private static List<DriverAvailabilityDao> CreateEntityList(List<DriverAvailabilityRequestDto> uniqueList, DriverDao driver, ZoneId zone) {
        List<DriverAvailabilityDao> availabilities = uniqueList.stream()
                .map(dto -> {
                    DriverAvailabilityDao entity = new DriverAvailabilityDao();

                    entity.setDriver(driver);
                    entity.setAvailableAt(dto.getAvailableAt().atStartOfDay(zone));
                    entity.setStartTime(dto.getStartTime());
                    entity.setEndTime(dto.getEndTime());
                    entity.setStatus(StatusEnum.AVAILABLE);

                    return entity;
                })
                .toList();
        return availabilities;
    }

    private static List<DriverAvailabilityRequestDto> duplicateCheck(List<DriverAvailabilityRequestDto> dates) {
        List<DriverAvailabilityRequestDto> uniqueList =
                dates.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(
                                        d -> d.getAvailableAt(),   // key = LocalDate
                                        d -> d,
                                        (a, b) -> a,               // merge strategy: keep first
                                        LinkedHashMap::new
                                ),
                                map -> new ArrayList<>(map.values())
                        ));
        return uniqueList;
    }
}