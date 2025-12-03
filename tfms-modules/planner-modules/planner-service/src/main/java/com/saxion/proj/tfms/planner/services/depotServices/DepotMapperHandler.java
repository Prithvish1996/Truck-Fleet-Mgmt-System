package com.saxion.proj.tfms.planner.services.depotServices;

import com.saxion.proj.tfms.commons.model.DepotDao;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;
import com.saxion.proj.tfms.planner.dto.LocationResponseDto;
import org.springframework.stereotype.Component;

@Component
public class DepotMapperHandler {

    public DepotResponseDto toDto(DepotDao depot) {

        DepotResponseDto dto = new DepotResponseDto();
        dto.setId(depot.getId());
        dto.setName(depot.getName());
        dto.setCreatedAt(depot.getCreatedAt());
        dto.setUpdatedAt(depot.getUpdatedAt());
        dto.setLocation(toLocationResponseDto(depot.getLocation()));

        return dto;
    }

    // Location mapping helpers
    private LocationResponseDto toLocationResponseDto(LocationDao location) {
        if (location == null) return null;

        LocationResponseDto dto = new LocationResponseDto();
        dto.setAddress(location.getAddress());
        dto.setPostcode(location.getPostalCode());
        dto.setCity(location.getCity());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        return dto;
    }
}
