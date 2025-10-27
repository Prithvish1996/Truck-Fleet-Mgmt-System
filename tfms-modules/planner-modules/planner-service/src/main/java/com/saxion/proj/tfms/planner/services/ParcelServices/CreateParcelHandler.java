package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
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

        // Check if parcel name exists
        if (parcelRepository.existsByName(dto.getName())) {
            return ApiResponse.error("Parcel name already exists");
        }

        // --- Handle Warehouse ---
        WareHouseDao warehouse;
        if (dto.getWarehouse() == null) {
            return ApiResponse.error("Warehouse info is required");
        }

        // Check if warehouse exists by name
        warehouse = warehouseRepository.findByName(dto.getWarehouse().getName())
                .orElseGet(() -> {
                    // Create warehouse if not exist
                    WareHouseDao newWarehouse = new WareHouseDao();
                    newWarehouse.setName(dto.getWarehouse().getName());

                    // Handle warehouse location
                    LocationDao warehouseLocation = handleLocation(dto.getWarehouse().getLocation());
                    newWarehouse.setLocation(warehouseLocation);

                    return warehouseRepository.save(newWarehouse);
                });

        // --- Handle Delivery Location ---
        LocationDao deliveryLocation = handleLocation(dto.getDeliveryLocation());

        // --- Create Parcel ---
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

        parcelRepository.save(parcel);

        return ApiResponse.success(parcelMapper.toDto(parcel));
    }

    /**
     * Handles location creation or retrieval by postcode
     */
    private LocationDao handleLocation(com.saxion.proj.tfms.planner.dto.LocationRequestDto dto) {
        if (dto == null) throw new RuntimeException("Location info is required");

        // Try to find existing location by postcode
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