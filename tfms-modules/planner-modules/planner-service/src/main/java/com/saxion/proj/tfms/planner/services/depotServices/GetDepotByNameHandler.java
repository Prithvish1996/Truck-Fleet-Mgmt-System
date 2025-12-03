package com.saxion.proj.tfms.planner.services.depotServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IGetDepotByName;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;
import com.saxion.proj.tfms.planner.repository.DepotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Qualifier("getDepotByNameHandler")
public class GetDepotByNameHandler implements IGetDepotByName {

    private final DepotRepository depotRepository;
    private final DepotMapperHandler mapper;

    @Autowired
    public GetDepotByNameHandler(DepotRepository depotRepository,
                               DepotMapperHandler mapper) {
        this.depotRepository = depotRepository;
        this.mapper = mapper;
    }

    @Override
    public ApiResponse<DepotResponseDto> Hanle(String depotName) {
        if (depotName == null || depotName.equals(""))
            return ApiResponse.error("Invalid depot name");

        DepotDao depot = depotRepository.findByName(depotName).orElse(null);
        if (depot == null)
            return ApiResponse.error("depot not found");

        DepotResponseDto dto = mapper.toDto(depot);

        return ApiResponse.success(dto);
    }
}
