package com.saxion.proj.tfms.planner.services.depotServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IGetDepotById;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;
import com.saxion.proj.tfms.planner.repository.DepotRepository;
import com.saxion.proj.tfms.planner.services.warehouseServices.WarehouseMapperHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Qualifier("getDepotByIdHandler")
public class GetDepotByIdHandler implements IGetDepotById {

    private final DepotRepository depotRepository;
    private final DepotMapperHandler mapper;

    @Autowired
    public GetDepotByIdHandler(DepotRepository depotRepository,
                               DepotMapperHandler mapper) {
        this.depotRepository = depotRepository;
        this.mapper = mapper;
    }

    @Override
    public ApiResponse<DepotResponseDto> Handle(Long depotId) {
        if (depotId == null || depotId <= 0)
            return ApiResponse.error("Invalid depot ID");

        DepotDao depot = depotRepository.findById(depotId).orElse(null);
        if (depot == null)
            return ApiResponse.error("depot not found");

        DepotResponseDto dto = mapper.toDto(depot);

        return ApiResponse.success(dto);
    }
}
