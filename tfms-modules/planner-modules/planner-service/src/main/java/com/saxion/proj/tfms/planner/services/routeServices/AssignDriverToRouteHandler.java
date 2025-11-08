package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IAssignDriverToRoute;
import com.saxion.proj.tfms.planner.dto.AssignRouteRequestDto;
import com.saxion.proj.tfms.planner.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
@Qualifier("assignDriverToRoute")
public class AssignDriverToRouteHandler implements IAssignDriverToRoute {
    private final RouteRepository routeRepository;
    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;
    private final RouteStopRepository routeStopRepository;
    private final NotificationService notificationService;
    private final ParcelRepository parcelRepository;

    public AssignDriverToRouteHandler(RouteRepository routeRepository,
                                      TruckRepository truckRepository,
                                      DriverRepository driverRepository,
                                      RouteStopRepository routeStopRepository,
                                      NotificationService notificationService,
                                      ParcelRepository parcelRepository
    ) {
        this.routeRepository = routeRepository;
        this.truckRepository = truckRepository;
        this.driverRepository = driverRepository;
        this.routeStopRepository = routeStopRepository;
        this.notificationService = notificationService;
        this.parcelRepository = parcelRepository;
    }


    /**
     * Assigns a driver and truck to a route, and updates stop priorities.
     */
    @Override
    public ApiResponse<String> handle(AssignRouteRequestDto dto) {

        // Retrieve route, truck, driver
        Optional<RouteDao> routeOpt = routeRepository.findById(dto.getRoutId());
        if (routeOpt.isEmpty()) {
            return ApiResponse.error("Route not found for ID: " + dto.getRoutId());
        }
        RouteDao route = routeOpt.get();

        Optional<TruckDao> truckOpt = truckRepository.findById(dto.getTruckId());
        if (truckOpt.isEmpty()) {
            return ApiResponse.error("Truck not found for ID: " + dto.getTruckId());
        }

        Optional<DriverDao> driverOpt = driverRepository.findById(dto.getDriverId());
        if (driverOpt.isEmpty()) {
            return ApiResponse.error("Driver not found for ID: " + dto.getDriverId());
        }

        TruckDao truck = truckOpt.get();
        DriverDao driver = driverOpt.get();

        // Update route if truck/driver changed
        boolean updated = false;
        if (route.getTruck() == null || !route.getTruck().getId().equals(truck.getId())) {
            route.setTruck(truck);
            updated = true;
        }

        if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
            route.setDriver(driver);
            updated = true;
        }

        if (updated) {
            route.setStatus(StatusEnum.ASSIGNED);
            routeRepository.save(route);
        }

        // Update stops priority
        if (dto.getStops() != null && !dto.getStops().isEmpty()) {
            dto.getStops().forEach(stopDto -> {
                routeStopRepository.findById(stopDto.getStopId()).ifPresent(stop -> {
                    stop.setPriority(stopDto.getPriority());
                    routeStopRepository.save(stop);

                    // Update each parcel on this stop to ASSIGNED
                    if (stop.getParcels() != null && !stop.getParcels().isEmpty()) {
                        stop.getParcels().forEach(parcel -> {
                            parcel.setStatus(StatusEnum.ASSIGNED);
                            parcelRepository.save(parcel);
                        });
                    }
                });
            });
        }

        // Send notification to driver (via reusable component)
        notificationService.sendDriverAssignmentNotification(driver, route);

        return ApiResponse.success("Driver and truck successfully assigned to route.");
    }
}
