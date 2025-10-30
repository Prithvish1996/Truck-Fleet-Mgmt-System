package com.saxion.proj.tfms.planner.services.WarehouseServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.WarehouseServices.IListWarehouses;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListWarehousesHandler implements IListWarehouses {

    private final WarehouseRepository warehouseRepository;
    private final ParcelRepository parcelRepository;
    private final WarehouseMapperHandler mapper;

    //

    @Autowired
    public ListWarehousesHandler(WarehouseRepository warehouseRepository,
                                 ParcelRepository parcelRepository,
                                 WarehouseMapperHandler mapper) {
        this.warehouseRepository = warehouseRepository;
        this.parcelRepository = parcelRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> Handle(Pageable pageable) {

        Page<WareHouseDao> page = warehouseRepository.findByActiveTrue(pageable);

        List<WareHouseResponseDto> dtos = page.stream()
                .map(wh -> {
                    WareHouseResponseDto dto = mapper.toDto(wh,0L,0L,0L);
                    dto.setPendingParcels(parcelRepository.countByWarehouseAndStatus(wh, StatusEnum.PENDING));
                    dto.setScheduledParcels(parcelRepository.countByWarehouseAndStatus(wh, StatusEnum.SCHEDULED));
                    dto.setDeliveredParcels(parcelRepository.countByWarehouseAndStatus(wh, StatusEnum.DELIVERED));
                    return dto;

                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("data", dtos);

        return ApiResponse.success(response);
    }
}
