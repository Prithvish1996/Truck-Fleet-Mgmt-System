package com.saxion.proj.tfms.planner.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class WareHouseResponseDto {
    private Long id;
    private String name;
    private LocationResponseDto location;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Parcel counts
    private long pendingParcels;
    private long scheduledParcels;
    private long deliveredParcels;
}