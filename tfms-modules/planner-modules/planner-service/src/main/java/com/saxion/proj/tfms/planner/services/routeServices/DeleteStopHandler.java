package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.RouteStopDao;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IDeleteStop;
import com.saxion.proj.tfms.planner.repository.*;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@Qualifier("deleteStopHandler")
public class DeleteStopHandler implements IDeleteStop {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final ParcelRepository parcelRepository;

    @Autowired
    public DeleteStopHandler(RouteStopRepository routeStopRepository,
                             RouteRepository routeRepository,
                             ParcelRepository parcelRepository) {
        this.routeStopRepository = routeStopRepository;
        this.routeRepository = routeRepository;
        this.parcelRepository = parcelRepository;
    }

    //parcelId The ID of the parcel to delete.
    @Override
    public ApiResponse<Void> handle(Long stopId) {

        if (stopId == null || stopId <= 0) {
            return ApiResponse.error("Invalid stop ID");
        }

        RouteStopDao stop = routeStopRepository.findById(stopId).orElse(null);

        if (stop == null) {
            return ApiResponse.error("Stop not found");
        }

        // Rule: A stop CANNOT be deleted if any parcel is already delivered
        boolean hasDeliveredParcels = stop.getParcels()
                .stream()
                .anyMatch(p -> p.getStatus() == StatusEnum.DELIVERED ||
                        p.getStatus() == StatusEnum.COMPLETED);

        if (hasDeliveredParcels) {
            return ApiResponse.error("Stop cannot be deleted because one or more parcels have already been delivered.");
        }

        // Unassign parcels from this stop and reset to PENDING
        if (stop.getParcels() != null && !stop.getParcels().isEmpty()) {
            for (ParcelDao parcel : stop.getParcels()) {
                parcel.setStop(null);
                parcel.setStatus(StatusEnum.PENDING);   // ready to be rescheduled
            }
            parcelRepository.saveAll(stop.getParcels());
        }

        // Remove stop from parent route to avoid stale references
        if (stop.getRoute() != null) {
            stop.getRoute().getStops().remove(stop);
            routeRepository.save(stop.getRoute());
        }

        // Delete stop safely
        routeStopRepository.delete(stop);

        return ApiResponse.success(null, "Stop deleted successfully");
    }
}
