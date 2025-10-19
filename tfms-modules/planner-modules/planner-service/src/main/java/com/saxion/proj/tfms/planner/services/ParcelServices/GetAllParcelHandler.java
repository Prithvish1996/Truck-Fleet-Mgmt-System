package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.IGetAllParcels;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetAllParcelHandler implements IGetAllParcels {

    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;

    public GetAllParcelHandler(ParcelRepository parcelRepository, ParcelMapperHandler parcelMapper) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
    }

    @Override
    public ApiResponse<List<ParcelResponseDto>> Handle(Long warehouseId) {
        //Validate warehouseId
        if (warehouseId == null || warehouseId <= 0L) {  // 0L for long literal
            return ApiResponse.error("Invalid warehouse ID");
        }

        //Fetch parcels and filter by warehouseId
        List<ParcelResponseDto> parcelDtos = parcelRepository.findAll()
                .stream()
                .filter(parcel -> parcel.getWarehouse() != null &&
                                  parcel.getWarehouse().getId().equals(warehouseId))
                .map(parcelMapper::toDto)
                .collect(Collectors.toList());

        // Check if any parcels were found
        if (parcelDtos.isEmpty()) {
            return ApiResponse.error("No parcels found for the given warehouse ID");
        }

        return ApiResponse.success(parcelDtos);
    }

}
