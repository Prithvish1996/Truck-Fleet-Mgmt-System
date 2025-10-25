package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.ICreateParcel;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateParcelHandler implements ICreateParcel {
    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;

    public CreateParcelHandler(ParcelRepository parcelRepository,
                                ParcelMapperHandler parcelMapper,
                                WarehouseRepository warehouseRepository,
                               LocationRepository locationRepository) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public ApiResponse<ParcelResponseDto> Handle(ParcelRequestDto dto) {
        if (parcelRepository.existsByName(dto.getName())) {
            return ApiResponse.error("Parcel name already exists");
        }

        // Fetch warehouse by ID
        var warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + dto.getWarehouseId()));

        // Build LocationDao from request
        LocationDao location = new LocationDao();
        location.setAddress(dto.getAddress());
        location.setPostalCode(dto.getPostalCode());
        location.setCity(dto.getCity());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setAddress(dto.getAddress());
        locationRepository.save(location); // Save the location

        ParcelDao parcel = new ParcelDao();
        parcel.setName(dto.getName());
        parcel.setWeight(dto.getWeight());
        parcel.setWarehouse(warehouse);
        parcel.setStatus(StatusEnum.PENDING);
        parcel.setDeliveryInstructions(dto.getDeliveryInstructions());
        parcel.setRecipientName(dto.getRecipientName());
        parcel.setRecipientPhone(dto.getRecipientPhone());
        parcel.setDeliveryLocation(location); // assign location entity
        parcel.setActive(true);

        parcelRepository.save(parcel);
        return ApiResponse.success(parcelMapper.toDto(parcel));
    }
}