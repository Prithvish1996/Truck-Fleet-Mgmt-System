package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import org.springframework.stereotype.Component;

@Component
public class ParcelMapperHandler {

    public ParcelResponseDto toDto(ParcelDao parcel) {
        ParcelResponseDto dto = new ParcelResponseDto();
        dto.setParcelId(parcel.getId());
        dto.setName(parcel.getName());
        dto.setAddress(parcel.getAddress());
        dto.setCity(parcel.getCity());
        dto.setPostalcode(parcel.getPostalcode());
        dto.setWeight(parcel.getWeight());
        dto.setWarehouseId(parcel.getWarehouse().getId());
        dto.setStatus(parcel.getStatus().name());
        dto.setCreatedAt(parcel.getCreatedAt());
        dto.setDeliveryInstructions(parcel.getDeliveryInstructions());
        dto.setRecipientName(parcel.getRecipientName());
        dto.setRecipientPhone(parcel.getRecipientPhone());
        dto.setLatitude(parcel.getLatitude());
        dto.setLongitude(parcel.getLongitude());
        return dto;
    }
}
