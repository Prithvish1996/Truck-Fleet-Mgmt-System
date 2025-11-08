package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IGetRouteByDriverId;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IGetRouteById;
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
@Qualifier("getRouteById")
public class GetRouteByIdHandler implements IGetRouteById {

    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;

    public GetRouteByIdHandler(RouteRepository routeRepository,
                                     DriverRepository driverRepository
    ) {
        this.routeRepository = routeRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Retrieves all assigned routes for a given driver.
     */
    public ApiResponse<RouteResponseDto> handle(Long id) {

        // Retrieve all routes by Id
        Optional<RouteDao> routes = routeRepository.findById(id);
        if (routes.isEmpty()) {
            return ApiResponse.error("No routes found with the id.");
        }
        RouteDao routeInfo = routes.get();

        // Map RouteDao → RouteResponseDto
        RouteResponseDto responseDto = RouteResponseDto.fromEntity(routeInfo);

        // Wrap and return
        return ApiResponse.success(responseDto);
    }
}

