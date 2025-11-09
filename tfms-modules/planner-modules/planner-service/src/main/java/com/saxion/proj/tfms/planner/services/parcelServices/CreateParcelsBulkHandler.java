package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.parcelServices.ICreateParcelsBulk;
import com.saxion.proj.tfms.planner.dto.LocationRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Qualifier("createParcelsBulkHandler")
@Transactional
public class CreateParcelsBulkHandler implements ICreateParcelsBulk {

    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;

    public CreateParcelsBulkHandler(ParcelRepository parcelRepository,
                                    ParcelMapperHandler parcelMapper,
                                    WarehouseRepository warehouseRepository,
                                    LocationRepository locationRepository) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public ApiResponse<List<ParcelResponseDto>> Handle(List<ParcelRequestDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return ApiResponse.error("Parcel list cannot be empty");
        }

        List<String> duplicateNames = dtos.stream()
                .map(ParcelRequestDto::getName)
                .filter(parcelRepository::existsByName)
                .collect(Collectors.toList());

        if (!duplicateNames.isEmpty()) {
            return ApiResponse.error("Some parcel names already exist: " + String.join(", ", duplicateNames));
        }

        List<ParcelDao> parcelDaos = new ArrayList<>();

        for (ParcelRequestDto dto : dtos) {
            // --- Validate warehouse ---
            if (dto.getWarehouse() == null) {
                return ApiResponse.error("Warehouse info is required for parcel: " + dto.getName());
            }

            WareHouseDao warehouse = warehouseRepository.findByName(dto.getWarehouse().getName())
                    .orElseGet(() -> {
                        WareHouseDao newWarehouse = new WareHouseDao();
                        newWarehouse.setName(dto.getWarehouse().getName());
                        newWarehouse.setLocation(handleLocation(dto.getWarehouse().getLocation()));
                        return warehouseRepository.save(newWarehouse);
                    });

            // --- Handle delivery location ---
            LocationDao deliveryLocation = handleLocation(dto.getDeliveryLocation());

            // --- Build Parcel ---
            ParcelDao parcel = new ParcelDao();
            parcel.setName(dto.getName());
            parcel.setWeight(dto.getWeight());
            parcel.setVolume(dto.getVolume());
            parcel.setWarehouse(warehouse);
            parcel.setStatus(StatusEnum.PENDING);
            parcel.setDeliveryInstructions(dto.getDeliveryInstructions());
            parcel.setRecipientName(dto.getRecipientName());
            parcel.setRecipientPhone(dto.getRecipientPhone());
            parcel.setDeliveryLocation(deliveryLocation);
            parcel.setActive(true);

            parcelDaos.add(parcel);
        }

        // Perform single batch save
        List<ParcelDao> savedParcels = parcelRepository.saveAll(parcelDaos);

        // Map to response DTOs
        List<ParcelResponseDto> responseDtos = savedParcels.stream()
                .map(parcelMapper::toDto)
                .collect(Collectors.toList());

        return ApiResponse.success(responseDtos, "All parcels created successfully");
    }

    /**
     * Handles location creation or retrieval by postcode
     */
    private LocationDao handleLocation(LocationRequestDto dto) {
        if (dto == null) throw new RuntimeException("Location info is required");

        return locationRepository.findByPostalCode(dto.getPostcode())
                .orElseGet(() -> {
                    LocationDao location = new LocationDao();
                    location.setAddress(dto.getAddress());
                    location.setCity(dto.getCity());
                    location.setLatitude(dto.getLatitude());
                    location.setLongitude(dto.getLongitude());
                    location.setPostalCode(dto.getPostcode());
                    return locationRepository.save(location);
                });
    }
}

