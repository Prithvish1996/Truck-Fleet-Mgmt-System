package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.planner.abstractions.driverServices.IGetAvailableDrivers;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("getAvailableDriversHandler")
@Transactional
public class GetAvailableDriversHandler implements IGetAvailableDrivers {

    private final DriverRepository driverRepository;

    public GetAvailableDriversHandler(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public ApiResponse<List<DriverResponseDto>> Handle() {
        // 1. Fetch available drivers
        List<DriverDao> availableDrivers = driverRepository.findByIsAvailableTrue();

        // 2. Map to DTO using your static mapper
        List<DriverResponseDto> response = availableDrivers.stream()
                .map(DriverResponseDto::fromEntity)
                .toList();

        return ApiResponse.success(response, "Available drivers retrieved successfully");
    }
}
