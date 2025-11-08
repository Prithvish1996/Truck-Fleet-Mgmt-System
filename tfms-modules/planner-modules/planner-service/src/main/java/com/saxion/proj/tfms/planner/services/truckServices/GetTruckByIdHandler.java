package com.saxion.proj.tfms.planner.services.truckServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.planner.abstractions.truckServices.IGetTruckById;
import com.saxion.proj.tfms.planner.dto.TruckResponseDto;
import com.saxion.proj.tfms.planner.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Qualifier("getTruckByIdHandler")
@Transactional
public class GetTruckByIdHandler implements IGetTruckById {

    private final TruckRepository truckRepository;

    public GetTruckByIdHandler(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    @Override
    public ApiResponse<TruckResponseDto> Handle(Long truckId) {
        if (truckId == null || truckId <= 0) {
            return ApiResponse.error("Invalid truck ID");
        }

        Optional<TruckDao> truckOpt = truckRepository.findById(truckId);

        if (truckOpt.isEmpty()) {
            return ApiResponse.error("Truck not found");
        }

        TruckDao truck = truckOpt.get();
        TruckResponseDto response = new TruckResponseDto(
                truck.getId(),
                truck.getPlateNumber(),
                truck.getType(),
                truck.getMake(),
                truck.getLastServiceDate(),
                truck.getLastServicedBy(),
                truck.getVolume(),
                truck.getIsAvailable(),
                truck.getRoutes()!= null ? truck.getRoutes().size() : 0,
                truck.getAssignments() != null ? truck.getAssignments().size() : 0);

        return ApiResponse.success(response);
    }
}
