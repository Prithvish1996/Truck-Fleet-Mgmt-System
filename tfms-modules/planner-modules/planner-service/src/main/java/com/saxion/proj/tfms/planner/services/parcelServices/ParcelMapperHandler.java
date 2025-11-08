package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import org.springframework.stereotype.Component;

@Component
public class ParcelMapperHandler {

//    private String latitude;
//    private String longitude;

    public ParcelResponseDto toDto(ParcelDao parcel) {
        ParcelResponseDto dto = new ParcelResponseDto();
        dto.setParcelId(parcel.getId());
        dto.setName(parcel.getName());
        dto.setWeight(parcel.getWeight());
        dto.setVolume(parcel.getVolume());
        dto.setStatus(parcel.getStatus() != null ? parcel.getStatus().name() : null);
        dto.setCreatedAt(parcel.getCreatedAt());
        dto.setRecipientName(parcel.getRecipientName());
        dto.setRecipientPhone(parcel.getRecipientPhone());
        dto.setDeliveryInstructions(parcel.getDeliveryInstructions());

        // Delivery Location (null-safe)
        if (parcel.getDeliveryLocation() != null) {
            dto.setDeliveryAddress(parcel.getDeliveryLocation().getAddress());
            dto.setDeliveryPostalCode(parcel.getDeliveryLocation().getPostalCode());
            dto.setDeliveryCity(parcel.getDeliveryLocation().getCity());
            dto.setDeliveryLatitude(parcel.getDeliveryLocation().getLatitude());
            dto.setDeliveryLongitude(parcel.getDeliveryLocation().getLongitude());
        }

        // Warehouse (null-safe)
        if (parcel.getWarehouse() != null) {
            dto.setWarehouseId(parcel.getWarehouse().getId());

            // Warehouse location (null-safe)
            if (parcel.getWarehouse().getLocation() != null) {
                dto.setWarehouseAddress(parcel.getWarehouse().getLocation().getAddress());
                dto.setWarehousePostalCode(parcel.getWarehouse().getLocation().getPostalCode());
                dto.setWarehouseCity(parcel.getWarehouse().getLocation().getCity());
                dto.setWarehouseLatitude(parcel.getWarehouse().getLocation().getLatitude());
                dto.setWarehouseLongitude(parcel.getWarehouse().getLocation().getLongitude());
            }
        }

        return dto;
    }
}
