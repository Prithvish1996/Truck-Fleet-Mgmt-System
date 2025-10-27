package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.IUpdateParcel;
import com.saxion.proj.tfms.planner.dto.LocationRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.Location;
import java.util.Optional;

@Service
public class UpdateParcelHandler implements IUpdateParcel {
    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public UpdateParcelHandler(ParcelRepository parcelRepository,
                               ParcelMapperHandler parcelMapper,
                               WarehouseRepository warehouseRepository,
                               LocationRepository locationRepository) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    @Transactional
    public ApiResponse<ParcelResponseDto> Handle(Long parcelId, ParcelRequestDto dto) {

        // --- Validate parcel ID ---
        if (parcelId == null || parcelId <= 0) {
            return ApiResponse.error("Invalid parcel ID");
        }

        Optional<ParcelDao> parcelOpt = parcelRepository.findByIdWithRelations(parcelId);
        if (parcelOpt.isEmpty()) {
            return ApiResponse.error("Parcel not found");
        }

        // --- Fetch existing parcel ---
        ParcelDao parcel = parcelOpt.get();

        // --- Validate name uniqueness ---
        if (!parcel.getName().equals(dto.getName()) && parcelRepository.existsByName(dto.getName())) {
            return ApiResponse.error("Parcel name already exists");
        }

        // --- Handle Warehouse ---
        WareHouseDao warehouse;
        WareHouseRequestDto warehouseDto = dto.getWarehouse();
        if (warehouseDto == null) {
            return ApiResponse.error("Warehouse information is required");
        }

        warehouse = warehouseRepository.findByName(warehouseDto.getName())
                .orElseGet(() -> {
                    WareHouseDao newWarehouse = new WareHouseDao();
                    newWarehouse.setName(warehouseDto.getName());
                    LocationDao warehouseLocation = handleLocation(warehouseDto.getLocation());
                    newWarehouse.setLocation(warehouseLocation);
                    return warehouseRepository.save(newWarehouse);
                });

        // --- Handle Delivery Location ---
        LocationDao deliveryLocation = handleLocation(dto.getDeliveryLocation());

        // --- Update parcel fields ---
        parcel.setName(dto.getName());
        parcel.setWeight(dto.getWeight());
        parcel.setVolume(dto.getVolume());
        parcel.setWarehouse(warehouse);
        parcel.setDeliveryInstructions(dto.getDeliveryInstructions());
        parcel.setRecipientName(dto.getRecipientName());
        parcel.setRecipientPhone(dto.getRecipientPhone());
        parcel.setDeliveryLocation(deliveryLocation);

        parcelRepository.save(parcel);

        return ApiResponse.success(parcelMapper.toDto(parcel));
    }

    /**
     * Handles location creation or reuse based on postcode
     */
    private LocationDao handleLocation(LocationRequestDto dto) {
        if (dto == null) throw new RuntimeException("Location information is required");

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
