package com.saxion.proj.tfms.planner.services.depotServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IUpdateDepot;
import com.saxion.proj.tfms.planner.dto.DepotRequestDto;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;
import com.saxion.proj.tfms.planner.repository.DepotRepository;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Qualifier("UpdateDepotHandler")
public class UpdateDepotHandler implements IUpdateDepot {

    private final DepotRepository depotRepository;
    private final DepotMapperHandler mapper;
    private final LocationRepository locationRepository;

    @Autowired
    public UpdateDepotHandler(DepotRepository depotRepository,
                              DepotMapperHandler mapper,
                              LocationRepository locationRepository) {
        this.depotRepository = depotRepository;
        this.mapper = mapper;
        this.locationRepository = locationRepository;
    }

    @Override
    public ApiResponse<DepotResponseDto> Handle(Long depotId, DepotRequestDto dto) {
        if (depotId == null || depotId <= 0)
            return ApiResponse.error("Invalid depot ID");

        DepotDao depot = depotRepository.findById(depotId).orElse(null);
        if (depot == null)
            return ApiResponse.error("Depot not found");

        depot.setName(dto.getName());

        depot.setLocation(locationRepository.findByPostalCode(dto.getLocation().getPostcode())
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

        depotRepository.save(depot);

        DepotResponseDto responseDto = mapper.toDto(depot);

        return ApiResponse.success(responseDto, "Depot updated successfully");
    }

}
