package com.saxion.proj.tfms.planner.services.warehouseServices;

import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.LocationResponseDto;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapperHandler {

    public WareHouseResponseDto toDto(WareHouseDao warehouse,
                                      long pendingCount,
                                      long scheduledCount,
                                      long deliveredCount) {

        WareHouseResponseDto dto = new WareHouseResponseDto();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setCreatedAt(warehouse.getCreatedAt());
        dto.setUpdatedAt(warehouse.getUpdatedAt());
        dto.setLocation(toLocationResponseDto(warehouse.getLocation()));
        dto.setPendingParcels(pendingCount);
        dto.setDeliveredParcels(deliveredCount);
        dto.setScheduledParcels(scheduledCount);

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
