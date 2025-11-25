package com.saxion.proj.tfms.planner.dto;

import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import lombok.Data;

@Data
public class LocationResponseDto {
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String postcode;

    public static LocationResponseDto fromEntity(LocationDao l) {
        LocationResponseDto dto = new LocationResponseDto();
        dto.setLatitude(l.getLatitude());
        dto.setLongitude(l.getLongitude());
        dto.setAddress(l.getAddress());
        dto.setCity(l.getCity());
        dto.setPostcode(l.getPostalCode());

        return dto;
    }
}
