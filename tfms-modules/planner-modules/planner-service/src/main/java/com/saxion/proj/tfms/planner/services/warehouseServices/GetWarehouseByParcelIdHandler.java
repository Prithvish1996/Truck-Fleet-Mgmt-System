package com.saxion.proj.tfms.planner.services.warehouseServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IGetWarehouseById;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IGetWarehouseByParcelId;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Qualifier("getWarehouseByParcelIdHandler")
public class GetWarehouseByParcelIdHandler implements IGetWarehouseByParcelId {

    private final WarehouseRepository warehouseRepository;
    private final ParcelRepository parcelRepository;
    private final WarehouseMapperHandler mapper;

    @Autowired
    public GetWarehouseByParcelIdHandler(WarehouseRepository warehouseRepository,
                                   ParcelRepository parcelRepository,
                                   WarehouseMapperHandler mapper) {
        this.warehouseRepository = warehouseRepository;
        this.parcelRepository = parcelRepository;
        this.mapper = mapper;
    }

    @Override
    public ApiResponse<WareHouseResponseDto> Handle(Long parcelId) {
        if (parcelId == null || parcelId <= 0)
            return ApiResponse.error("Invalid parcel ID");

        WareHouseDao warehouse = warehouseRepository.findWarehouseByParcelId(parcelId).orElse(null);
        if (warehouse == null)
            return ApiResponse.error("Warehouse not found");

        WareHouseResponseDto dto = mapper.toDto(warehouse, 0L,0L, 0L);
        dto.setPendingParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.PENDING));
        dto.setScheduledParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.SCHEDULED));
        dto.setDeliveredParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.DELIVERED));

        return ApiResponse.success(dto);
    }
}