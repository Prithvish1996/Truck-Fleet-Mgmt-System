package com.saxion.proj.tfms.planner.dto;

import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ParcelResponseDto {
    private Long parcelId;
    private String name;
    private Double weight;
    private Double volume;
    private String status;
    private ZonedDateTime createdAt;
    private String recipientName;
    private String recipientPhone;
    private String deliveryInstructions;
    private String deliveryAddress;
    private String deliveryPostalCode;
    private String deliveryCity;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private Long warehouseId;
    private String warehouseAddress;
    private String warehousePostalCode;
    private String warehouseCity;
    private Double warehouseLatitude;
    private Double warehouseLongitude;
}
