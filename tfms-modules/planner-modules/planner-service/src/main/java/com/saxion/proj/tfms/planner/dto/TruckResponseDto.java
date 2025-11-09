package com.saxion.proj.tfms.planner.dto;

import com.saxion.proj.tfms.commons.constants.TruckType;
import com.saxion.proj.tfms.commons.model.TruckDao;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TruckResponseDto {
    private Long truckId;
    private String plateNumber;
    private TruckType type;
    private String make;
    private LocalDate lastServiceDate;
    private String lastServicedBy;
    private Double volume;
    private Boolean isAvailable;
    private int numberOfRoutes;
    private int numberOfAssignment;

    public TruckResponseDto(Long id,
                            String plateNumber,
                            TruckType type,
                            String make,
                            LocalDate lastServiceDate,
                            String lastServicedBy,
                            Double volume,
                            boolean isAvailable,
                            int numberOfRoutes,
                            int numberOfAssignment) {
        this.truckId = id;
        this.plateNumber = plateNumber;
        this.type = type;
        this.make = make;
        this.lastServiceDate = lastServiceDate;
        this.lastServicedBy = lastServicedBy;
        this.volume = volume;
        this.isAvailable = isAvailable;
        this.numberOfRoutes = numberOfRoutes;
        this.numberOfAssignment = numberOfAssignment;
    }

    /**
     * Map from TruckDao entity to DTO
     */
    public static TruckResponseDto fromEntity(TruckDao truck) {
        if (truck == null) return null;

        int routes = truck.getRoutes() != null ? truck.getRoutes().size() : 0;
        int assignments = 0; // optionally calculate number of assigned parcels if needed

        return new TruckResponseDto(
                truck.getId(),
                truck.getPlateNumber(),
                truck.getType(),
                truck.getMake(),
                truck.getLastServiceDate(),
                truck.getLastServicedBy(),
                truck.getVolume(),
                truck.getIsAvailable(),
                routes,
                assignments
        );
    }
}
