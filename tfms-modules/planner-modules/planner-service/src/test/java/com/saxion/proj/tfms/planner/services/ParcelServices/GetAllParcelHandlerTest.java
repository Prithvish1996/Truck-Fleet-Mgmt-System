package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import org.junit.jupiter.api.Test;

import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GetAllParcelHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapperHandler parcelMapper;

    @InjectMocks
    private GetAllParcelHandler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test case 1: warehouseId is null
    @Test
    void handle_NullWarehouseId_ShouldReturnError() {
        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(null);
        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
    }

    // Test case 2: warehouseId <= 0
    @Test
    void handle_NonPositiveWarehouseId_ShouldReturnError() {
        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(0L);
        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
    }

    // Test case 3: warehouseId valid but no parcels found
    @Test
    void handle_ValidWarehouseId_NoParcels_ShouldReturnError() {
        Long warehouseId = 1L;

        when(parcelRepository.findAll()).thenReturn(Collections.emptyList());

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(warehouseId);

        assertFalse(response.isSuccess());
        assertEquals("No parcels found for the given warehouse ID", response.getMessage());
    }

    // Test case 4: warehouseId valid and parcels exist
    @Test
    void handle_ValidWarehouseId_ParcelsExist_ShouldReturnSuccess() {
        Long warehouseId = 1L;

        WareHouseDao warehouse = new WareHouseDao();
        warehouse.setId(warehouseId);

        ParcelDao parcel = new ParcelDao();
        parcel.setWarehouse(warehouse);

        when(parcelRepository.findAll()).thenReturn(List.of(parcel));

        ParcelResponseDto dto = new ParcelResponseDto();
        when(parcelMapper.toDto(parcel)).thenReturn(dto);

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(warehouseId);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertSame(dto, response.getData().get(0));
    }
}