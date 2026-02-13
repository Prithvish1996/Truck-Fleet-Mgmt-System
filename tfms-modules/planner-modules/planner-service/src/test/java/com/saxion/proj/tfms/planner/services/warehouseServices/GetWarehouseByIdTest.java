package com.saxion.proj.tfms.planner.services.warehouseServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetWarehouseByIdTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private WarehouseMapperHandler mapper;

    @InjectMocks
    private GetWarehouseByIdHandler handler;

    private WareHouseDao warehouse;
    private WareHouseResponseDto dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        warehouse = new WareHouseDao();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");

        dto = new WareHouseResponseDto();
        dto.setId(1L);
        dto.setName("Main Warehouse");
    }

    /**
     * Test 1: warehouseId == null → should return "Invalid warehouse ID"
     */
    @Test
    void handle_NullWarehouseId_ShouldReturnInvalidIdError() {
        ApiResponse<WareHouseResponseDto> response = handler.Handle(null);

        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
        verifyNoInteractions(warehouseRepository, parcelRepository, mapper);
    }

    /**
     * Test 2: warehouseId <= 0 → should return "Invalid warehouse ID"
     */
    @Test
    void handle_NegativeWarehouseId_ShouldReturnInvalidIdError() {
        ApiResponse<WareHouseResponseDto> response = handler.Handle(0L);

        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
        verifyNoInteractions(warehouseRepository, parcelRepository, mapper);
    }

    /**
     * Test 3: Valid ID but warehouse not found → should return "Warehouse not found"
     */
    @Test
    void handle_WarehouseNotFound_ShouldReturnError() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<WareHouseResponseDto> response = handler.Handle(1L);

        assertFalse(response.isSuccess());
        assertEquals("Warehouse not found", response.getMessage());
        verify(warehouseRepository).findById(1L);
        verifyNoInteractions(parcelRepository, mapper);
    }

    /**
     * Test 4: Valid ID and warehouse exists → should return success with counts
     */
    @Test
    void handle_ValidWarehouse_ShouldReturnSuccessWithCounts() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(mapper.toDto(warehouse, 0L, 0L, 0L)).thenReturn(dto);

        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.PENDING)).thenReturn(3L);
        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.SCHEDULED)).thenReturn(2L);
        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.DELIVERED)).thenReturn(5L);

        ApiResponse<WareHouseResponseDto> response = handler.Handle(1L);

        assertTrue(response.isSuccess());
        assertEquals("Main Warehouse", response.getData().getName());
        assertEquals(3L, response.getData().getPendingParcels());
        assertEquals(2L, response.getData().getScheduledParcels());
        assertEquals(5L, response.getData().getDeliveredParcels());

        verify(warehouseRepository).findById(1L);
        verify(parcelRepository, times(3)).countByWarehouseAndStatus(eq(warehouse), any());
        verify(mapper).toDto(warehouse, 0L, 0L, 0L);
    }
}
