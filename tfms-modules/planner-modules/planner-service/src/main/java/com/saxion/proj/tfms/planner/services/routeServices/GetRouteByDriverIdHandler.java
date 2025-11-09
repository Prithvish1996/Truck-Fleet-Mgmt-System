package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IGetRouteByDriverId;
import com.saxion.proj.tfms.planner.dto.DriverRouteResponseDto;
import com.saxion.proj.tfms.planner.dto.RouteResponseDto;
import com.saxion.proj.tfms.planner.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Qualifier("getRouteByDriverId")
public class GetRouteByDriverIdHandler implements IGetRouteByDriverId {

    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;

    public GetRouteByDriverIdHandler(RouteRepository routeRepository,
                                      DriverRepository driverRepository
    ) {
        this.routeRepository = routeRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Retrieves all assigned routes for a given driver.
     */

    @Override
    public ApiResponse<DriverRouteResponseDto> handle(Long driverId) {
        // Validate driver
        Optional<DriverDao> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return ApiResponse.error("Driver not found for ID: " + driverId);
        }
        DriverDao driver = driverOpt.get();

        // Retrieve all routes assigned to driver with status = ASSIGNED
        List<RouteDao> driverRoutes = routeRepository.findAllByDriverIdAndStatus(driverId, StatusEnum.ASSIGNED);
        if (driverRoutes.isEmpty()) {
            return ApiResponse.error("No assigned routes found for this driver.");
        }

        // Map RouteDao → RouteResponseDto
        List<RouteResponseDto> mappedRoutes = driverRoutes.stream()
                .map(RouteResponseDto::fromEntity)
                .collect(Collectors.toList());

        // Build DriverRouteResponseDto
        DriverRouteResponseDto responseDto = new DriverRouteResponseDto();
        responseDto.setRoutes(mappedRoutes);

        // Wrap and return
        return ApiResponse.success(responseDto);
    }
}

