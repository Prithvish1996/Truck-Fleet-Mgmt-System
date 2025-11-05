package com.saxion.proj.tfms.planner.services.WarehouseServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.WarehouseServices.IGetWarehouseById;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GetWarehouseByIdHandler implements IGetWarehouseById {

    private final WarehouseRepository warehouseRepository;
    private final ParcelRepository parcelRepository;
    private final WarehouseMapperHandler mapper;

    @Autowired
    public GetWarehouseByIdHandler(WarehouseRepository warehouseRepository,
                                   ParcelRepository parcelRepository,
                                   WarehouseMapperHandler mapper) {
        this.warehouseRepository = warehouseRepository;
        this.parcelRepository = parcelRepository;
        this.mapper = mapper;
    }

    @Override
    public ApiResponse<WareHouseResponseDto> Handle(Long warehouseId) {
        if (warehouseId == null || warehouseId <= 0)
            return ApiResponse.error("Invalid warehouse ID");

        WareHouseDao warehouse = warehouseRepository.findById(warehouseId).orElse(null);
        if (warehouse == null)
            return ApiResponse.error("Warehouse not found");

        WareHouseResponseDto dto = mapper.toDto(warehouse, 0L,0L, 0L);
        dto.setPendingParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.PENDING));
        dto.setScheduledParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.SCHEDULED));
        dto.setDeliveredParcels(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.DELIVERED));

        return ApiResponse.success(dto);
    }
}
