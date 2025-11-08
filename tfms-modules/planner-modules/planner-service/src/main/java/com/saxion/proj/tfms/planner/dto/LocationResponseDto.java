package com.saxion.proj.tfms.planner.dto;

import lombok.Data;

@Data
public class LocationResponseDto {
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String postcode;
}
