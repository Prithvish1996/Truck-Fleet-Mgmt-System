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
    private String address;
    private String postalcode;
    private String city;
    private Double weight;
    private Long warehouseId;
    private String status;
    private ZonedDateTime createdAt;
    private String deliveryInstructions;
    private String recipientName;
    private String recipientPhone;
    private String latitude;
    private String longitude;
}
