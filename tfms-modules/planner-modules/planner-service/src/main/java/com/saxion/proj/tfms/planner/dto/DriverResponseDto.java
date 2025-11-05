package com.saxion.proj.tfms.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDto {
    private Long id;
    private String userName;
    private String email;
    private Boolean isAvailable;

    // Optional fields for displaying location info
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;

    public DriverResponseDto(Long id, String userName, String email, boolean isAvailable, String city, String address) {
        this.id = id;
        this.userName = userName;
        this.city = city;
        this.email = email;
        this.isAvailable = isAvailable;
        this.address = address;
    }
}
