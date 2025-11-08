package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.planner.abstractions.parcelServices.IUpdateParcelStatus;
import com.saxion.proj.tfms.planner.abstractions.routeServices.IUpdateRouteStatus;
import com.saxion.proj.tfms.planner.dto.UpdateParcelStatusRequestDto;
import com.saxion.proj.tfms.planner.dto.UpdateRouteStatusRequestDto;
import com.saxion.proj.tfms.planner.dto.routing.model.Parcel;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.RouteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
@Qualifier("updateParcelStatus")
public class UpdateParcelStatusHandler implements IUpdateParcelStatus {

    private final ParcelRepository parcelRepository;

    public UpdateParcelStatusHandler(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    /**
     * Updates the status of a parcel.
     */


    @Override
    public ApiResponse<String> handle(UpdateParcelStatusRequestDto dto) {
        // Validate parcel
        Optional<ParcelDao> parcelOpt = parcelRepository.findById(dto.getParcelId());
        if (parcelOpt.isEmpty()) {
            return ApiResponse.error("Parcel not found for ID: " + dto.getParcelId());
        }

        ParcelDao parcel = parcelOpt.get();

        // Convert string to StatusEnum safely
        StatusEnum newStatus;
        try {
            newStatus = StatusEnum.valueOf(dto.getStatus().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error("Invalid status: " + dto.getStatus());
        }

        // Update route status
        parcel.setStatus(newStatus);
        parcelRepository.save(parcel);

        return ApiResponse.success(
                "Successfully updated parcel #" + dto.getParcelId() + " to status: " + newStatus
        );
    }
}
