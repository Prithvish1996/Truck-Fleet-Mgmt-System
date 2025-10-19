package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.IUpdateParcel;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateParcelHandler implements IUpdateParcel {
    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public UpdateParcelHandler(ParcelRepository parcelRepository,
                               ParcelMapperHandler parcelMapper,
                               WarehouseRepository warehouseRepository) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
        this.warehouseRepository = warehouseRepository;
    }

    public ApiResponse<ParcelResponseDto> Handle(Long parcelId, ParcelRequestDto dto) {

        // Validate parcelId
        if (parcelId == null || parcelId <= 0) {
            return ApiResponse.error("Invalid parcel ID");
        }

        // Fetch the existing parcel
        Optional<ParcelDao> parcelOpt = parcelRepository.findById(parcelId);
        if (parcelOpt.isEmpty()) {
            return ApiResponse.error("Parcel not found");
        }
        ParcelDao parcel = parcelOpt.get();

        // Validate name uniqueness
        if (!parcel.getName().equals(dto.getName()) && parcelRepository.existsByName(dto.getName())) {
            return ApiResponse.error("Parcel name already exists");
        }

        // Fetch warehouse
        var warehouseOpt = warehouseRepository.findById(dto.getWarehouseId());
        if (warehouseOpt.isEmpty()) {
            return ApiResponse.error("Warehouse not found with ID: " + dto.getWarehouseId());
        }

        // Update fields
        parcel.setName(dto.getName());
        parcel.setAddress(dto.getAddress());
        parcel.setPostalcode(dto.getPostalCode());
        parcel.setWeight(dto.getWeight());
        parcel.setCity(dto.getCity());
        parcel.setWarehouse(warehouseOpt.get());
        parcel.setDeliveryInstructions(dto.getDeliveryInstructions());
        parcel.setRecipientName(dto.getRecipientName());
        parcel.setRecipientPhone(dto.getRecipientPhone());
        parcel.setLatitude(dto.getLatitude());
        parcel.setLongitude(dto.getLongitude());

        // Save updated parcel
        parcelRepository.save(parcel);

        return ApiResponse.success(parcelMapper.toDto(parcel));
    }
}
