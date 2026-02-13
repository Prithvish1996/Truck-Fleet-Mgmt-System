package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IGetUnassignedRoutes;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import com.saxion.proj.tfms.planner.dto.GenerateRouteResponseDto;
import com.saxion.proj.tfms.planner.dto.RouteResponseDto;
import com.saxion.proj.tfms.planner.dto.TruckResponseDto;
import com.saxion.proj.tfms.planner.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Qualifier("getUnassignedRoutes")
public class GetUnassignedRoutesHandler implements IGetUnassignedRoutes {

    private final RouteRepository routeRepository;
    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;

    public GetUnassignedRoutesHandler(RouteRepository routeRepository,
                                      TruckRepository truckRepository,
                                      DriverRepository driverRepository
    ) {
        this.routeRepository = routeRepository;
        this.truckRepository = truckRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public ApiResponse<GenerateRouteResponseDto> handle() {
        try {
            // Retrieve all PLANNED routes (unassigned routes)
            List<RouteDao> unassignedRoutes = routeRepository.findByStatus(StatusEnum.PLANNED);

            if (unassignedRoutes.isEmpty()) {
                return ApiResponse.error("No unassign route available.");
            }
            // Retrieve supporting data
            List<RouteResponseDto> unassignedRouteDtos = unassignedRoutes.stream()
                    .map(RouteResponseDto::fromEntity)
                    .collect(Collectors.toList());

            GenerateRouteResponseDto dto = new GenerateRouteResponseDto();
            dto.setAssignRoutes(null);
            dto.setUnAssignedRoute(unassignedRouteDtos);
            dto.setTrucks(truckRepository.findAllByIsAvailableTrue().stream()
                    .map(TruckResponseDto::fromEntity)
                    .collect(Collectors.toList()));
            dto.setDrivers(driverRepository.findByIsAvailableTrue().stream()
                    .map(DriverResponseDto::fromEntity)
                    .collect(Collectors.toList()));

            return ApiResponse.success(dto);

        } catch (Exception e) {
            return ApiResponse.error("Failed to retrieve unassigned routes: " + e.getMessage());
        }
    }
}