package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IGetRouteByDriverId;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IGetRouteByTruckId;
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
@Qualifier("getRouteByTruckId")
public class GetRouteByTruckIdHandler implements IGetRouteByTruckId {

    private final RouteRepository routeRepository;
    private final TruckRepository truckRepository;

    public GetRouteByTruckIdHandler(RouteRepository routeRepository,
                                    TruckRepository truckRepository
    ) {
        this.routeRepository = routeRepository;
        this.truckRepository = truckRepository;
    }

    /**
     * Retrieves all assigned routes for a given driver.
     */

    @Override
    public ApiResponse<DriverRouteResponseDto> handle(Long truckId) {
        // Validate truck
        Optional<TruckDao> truckOpt = truckRepository.findById(truckId);
        if (truckOpt.isEmpty()) {
            return ApiResponse.error("Truck not found for ID: " + truckId);
        }
        TruckDao truck = truckOpt.get();

        // Retrieve all routes assigned to truck with status = ASSIGNED
        List<RouteDao> truckRoutes = routeRepository.findAllByTruckIdAndStatus(truckId, StatusEnum.ASSIGNED);
        if (truckRoutes.isEmpty()) {
            return ApiResponse.error("No assigned routes found for this truck.");
        }

        // Map RouteDao → RouteResponseDto
        List<RouteResponseDto> mappedRoutes = truckRoutes.stream()
                .map(RouteResponseDto::fromEntity)
                .collect(Collectors.toList());

        // Build DriverRouteResponseDto
        DriverRouteResponseDto responseDto = new DriverRouteResponseDto();
        responseDto.setRoutes(mappedRoutes);

        // Wrap and return
        return ApiResponse.success(responseDto);
    }
}
