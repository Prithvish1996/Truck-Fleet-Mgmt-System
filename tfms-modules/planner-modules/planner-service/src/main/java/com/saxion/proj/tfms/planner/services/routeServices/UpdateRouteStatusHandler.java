package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IUpdateRouteStatus;
import com.saxion.proj.tfms.planner.dto.UpdateRouteStatusRequestDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.RouteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
@Qualifier("updateRouteStatus")
public class UpdateRouteStatusHandler implements IUpdateRouteStatus {

    private final RouteRepository routeRepository;
    private final ParcelRepository parcelRepository;

    public UpdateRouteStatusHandler(RouteRepository routeRepository,
                                    ParcelRepository parcelRepository) {
        this.routeRepository = routeRepository;
        this.parcelRepository = parcelRepository;
    }

    /**
     * Updates the status of a route and all its parcels.
     */


    @Override
    public ApiResponse<String> handle(UpdateRouteStatusRequestDto dto) {
        // Validate route
        Optional<RouteDao> routeOpt = routeRepository.findById(dto.getRouteId());
        if (routeOpt.isEmpty()) {
            return ApiResponse.error("Route not found for ID: " + dto.getRouteId());
        }

        RouteDao route = routeOpt.get();

        // Convert string to StatusEnum safely
        StatusEnum newStatus;
        try {
            newStatus = StatusEnum.valueOf(dto.getStatus().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error("Invalid status: " + dto.getStatus());
        }

        // Update route status
        route.setStatus(newStatus);
        routeRepository.save(route);

        // Update parcels under this route
        if (route.getStops() != null && !route.getStops().isEmpty()) {
            for (var stop : route.getStops()) {
                if (stop.getParcels() != null && !stop.getParcels().isEmpty()) {
                    for (ParcelDao parcel : stop.getParcels()) {
                        parcel.setStatus(newStatus);
                        parcelRepository.save(parcel);
                    }
                }
            }
        }

        return ApiResponse.success(
                "Successfully updated route #" + dto.getRouteId() + " and all associated parcels to status: " + newStatus
        );
    }
}
