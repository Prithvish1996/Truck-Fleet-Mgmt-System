package com.saxion.proj.tfms.planner.services.WarehouseServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.WarehouseServices.IUpdateWarehouse;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UpdateWarehouseHandler implements IUpdateWarehouse {

    private final WarehouseRepository warehouseRepository;
    private final ParcelRepository parcelRepository;
    private final WarehouseMapperHandler mapper;
    private final LocationRepository locationRepository;

    public UpdateWarehouseHandler(WarehouseRepository warehouseRepository,
                                  ParcelRepository parcelRepository,
                                  WarehouseMapperHandler mapper,
                                  LocationRepository locationRepository) {
        this.warehouseRepository = warehouseRepository;
        this.parcelRepository = parcelRepository;
        this.mapper = mapper;
        this.locationRepository = locationRepository;
    }

    @Override
    public ApiResponse<WareHouseResponseDto> Handle(Long warehouseId, WareHouseRequestDto dto) {
        if (warehouseId == null || warehouseId <= 0)
            return ApiResponse.error("Invalid warehouse ID");

        WareHouseDao warehouse = warehouseRepository.findById(warehouseId).orElse(null);
        if (warehouse == null)
            return ApiResponse.error("Warehouse not found");

        warehouse.setName(dto.getName());

        warehouse.setLocation(locationRepository.findByPostalCode(dto.getLocation().getPostcode())
                .orElseGet(() -> {
                    // create new location
                    LocationDao loc = new LocationDao();
                    loc.setAddress(dto.getLocation().getAddress());
                    loc.setCity(dto.getLocation().getCity());
                    loc.setLatitude(dto.getLocation().getLatitude());
                    loc.setLongitude(dto.getLocation().getLongitude());
                    loc.setPostalCode(dto.getLocation().getPostcode());
                    return locationRepository.save(loc);
                }));

        warehouseRepository.save(warehouse);

        WareHouseResponseDto responseDto = mapper.toDto(warehouse, 0L,0L, 0L);
        responseDto.setPendingParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.PENDING));
        responseDto.setScheduledParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.SCHEDULED));
        responseDto.setDeliveredParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.DELIVERED));

        return ApiResponse.success(responseDto, "Warehouse updated successfully");
    }
}
