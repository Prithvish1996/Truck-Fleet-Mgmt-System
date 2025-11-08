package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParcelMapperHandlerTest {

    private ParcelMapperHandler mapper;

    @BeforeEach
    void setUp() {
        mapper = new ParcelMapperHandler();
    }

    @Test
    void handle_ToDto() {
        // Arrange: create a ParcelDao with all fields populated
        WareHouseDao warehouse = new WareHouseDao();
        warehouse.setId(5L);

        ParcelDao parcel = new ParcelDao();
        parcel.setId(1L);
        parcel.setName("Test Parcel");
        parcel.setWeight(2.5);
        parcel.setWarehouse(warehouse);
        parcel.setStatus(StatusEnum.PENDING);
        parcel.setCreatedAt(ZonedDateTime.now());
        parcel.setDeliveryInstructions("Leave at front door");
        parcel.setRecipientName("John Doe");
        parcel.setRecipientPhone("+31 6 12345678");

        // Act
        ParcelResponseDto dto = mapper.toDto(parcel);

        // Assert: verify all fields are mapped correctly
        assertEquals(parcel.getId(), dto.getParcelId());
        assertEquals(parcel.getName(), dto.getName());
        assertEquals(parcel.getWeight(), dto.getWeight());
        assertEquals(parcel.getWarehouse().getId(), dto.getWarehouseId());
        assertEquals(parcel.getStatus().name(), dto.getStatus());
        assertEquals(parcel.getCreatedAt(), dto.getCreatedAt());
        assertEquals(parcel.getDeliveryInstructions(), dto.getDeliveryInstructions());
        assertEquals(parcel.getRecipientName(), dto.getRecipientName());
        assertEquals(parcel.getRecipientPhone(), dto.getRecipientPhone());
    }
}