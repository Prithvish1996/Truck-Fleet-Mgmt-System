package com.saxion.proj.tfms.planner.services.depotServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IListDepot;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IListWarehouses;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import com.saxion.proj.tfms.planner.repository.DepotRepository;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import com.saxion.proj.tfms.planner.services.warehouseServices.WarehouseMapperHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Qualifier("ListDepotHandler")
public class ListDepotHandler implements IListDepot {

    private final DepotRepository depotRepository;
    private final DepotMapperHandler mapper;

    @Autowired
    public ListDepotHandler(DepotRepository depotRepository,
                                 DepotMapperHandler mapper) {
        this.depotRepository = depotRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> Handle(Pageable pageable) {

        Page<DepotDao> page = depotRepository.findByIsActiveTrue(pageable);

        List<DepotResponseDto> dtos = page.stream()
                .map(wh -> {
                    DepotResponseDto dto = mapper.toDto(wh);
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
