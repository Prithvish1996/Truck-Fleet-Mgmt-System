package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.abstractions.parcelServices.IDeleteParcel;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Qualifier("deleteParcelHandler")
public class DeleteParcelHandler implements IDeleteParcel {

    private final ParcelRepository parcelRepository;

    @Autowired
    public DeleteParcelHandler(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    //parcelId The ID of the parcel to delete.
    @Override
    public ApiResponse<Void> Handle(Long parcelId) {

        // Validate parcelId
        if (parcelId == null || parcelId <= 0L) {
            return ApiResponse.error("Invalid parcel ID");
        }

        // Fetch parcel
        Optional<ParcelDao> parcelOpt = parcelRepository.findById(parcelId);
        if (parcelOpt.isEmpty()) {
            return ApiResponse.error("Parcel not found");
        }

        ParcelDao parcel = parcelOpt.get();

        // delete the parcel
        parcelRepository.delete(parcelOpt.get());

        return ApiResponse.success(null, "Parcel deleted successfully");
    }
}
