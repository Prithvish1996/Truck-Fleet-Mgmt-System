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

    public static ParcelResponseDto fromEntity(ParcelDao p) {
        ParcelResponseDto dto = new ParcelResponseDto();
        dto.setParcelId(p.getId());
        dto.setName(p.getName());
        dto.setWeight(p.getWeight());
        dto.setVolume(p.getVolume());
        dto.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        dto.setRecipientName(p.getRecipientName());
        dto.setRecipientPhone(p.getRecipientPhone());
        dto.setDeliveryInstructions(p.getDeliveryInstructions());

        if (p.getDeliveryLocation() != null) {
            dto.setDeliveryLatitude(p.getDeliveryLocation().getLatitude());
            dto.setDeliveryLongitude(p.getDeliveryLocation().getLongitude());
            dto.setDeliveryCity(p.getDeliveryLocation().getCity());
            dto.setDeliveryAddress(p.getDeliveryLocation().getAddress());
            dto.setDeliveryPostalCode(p.getDeliveryLocation().getPostalCode());
        }

        if (p.getWarehouse() != null) {
            dto.setWarehouseId(p.getWarehouse().getId());
            if (p.getWarehouse().getLocation() != null) {
                dto.setWarehouseAddress(p.getWarehouse().getLocation().getAddress());
                dto.setWarehouseLatitude(p.getWarehouse().getLocation().getLatitude());
                dto.setWarehouseLongitude(p.getWarehouse().getLocation().getLongitude());
                dto.setWarehouseCity(p.getWarehouse().getLocation().getCity());
                dto.setWarehousePostalCode(p.getWarehouse().getLocation().getPostalCode());
            }
        }

        return dto;
    }
}
