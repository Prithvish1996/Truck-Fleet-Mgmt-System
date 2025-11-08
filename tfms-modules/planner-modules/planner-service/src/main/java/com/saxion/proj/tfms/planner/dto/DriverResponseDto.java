package com.saxion.proj.tfms.planner.dto;

import com.saxion.proj.tfms.commons.model.DriverDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDto {
    private Long id;
    private String Name;
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

    /**
     * Map from DriverDao entity to DTO
     */
    public static DriverResponseDto fromEntity(DriverDao driver) {
        if (driver == null) return null;

        String city = driver.getLocation() != null ? driver.getLocation().getCity() : null;
        String address = driver.getLocation() != null ? driver.getLocation().getAddress() : null;

        return new DriverResponseDto(
                driver.getId(),
                driver.getUser().getUsername(),
                driver.getUser().getEmail(),
                driver.getIsAvailable(),
                city,
                address
        );
    }
}
