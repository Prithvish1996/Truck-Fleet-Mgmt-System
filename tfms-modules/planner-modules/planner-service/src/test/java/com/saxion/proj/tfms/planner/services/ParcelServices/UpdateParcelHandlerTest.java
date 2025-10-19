package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateParcelHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ParcelMapperHandler parcelMapper;

    @InjectMocks
    private UpdateParcelHandler updateParcelHandler;

    private ParcelDao parcel;
    private WareHouseDao warehouse;
    private ParcelRequestDto dto;
    private ParcelResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock data
        warehouse = new WareHouseDao();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");

        parcel = new ParcelDao();
        parcel.setId(1L);
        parcel.setName("Old Parcel");
        parcel.setWarehouse(warehouse);

        dto = new ParcelRequestDto();
        dto.setName("Updated Parcel");
        dto.setAddress("New Address");
        dto.setPostalCode("1234 AB");
        dto.setWeight(5.0);
        dto.setCity("Amsterdam");
        dto.setWarehouseId(1L);
        dto.setStatus("pending");
        dto.setDeliveryInstructions("Leave at door");
        dto.setRecipientName("John Doe");
        dto.setRecipientPhone("+31 6 1111 2222");
        dto.setLatitude("52.3676");
        dto.setLongitude("4.9041");

        responseDto = new ParcelResponseDto();
        responseDto.setName(dto.getName());
    }

    // Test 1: parcelId == null
    @Test
    void testHandle_NullParcelId_ShouldReturnError() {
        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(null, dto);

        assertFalse(response.isSuccess());
        assertEquals("Invalid parcel ID", response.getMessage());
        verify(parcelRepository, never()).findById(anyLong());
    }

    // Test 2: parcelId <= 0
    @Test
    void testHandle_InvalidParcelId_ShouldReturnError() {
        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(0L, dto);

        assertFalse(response.isSuccess());
        assertEquals("Invalid parcel ID", response.getMessage());
        verify(parcelRepository, never()).findById(anyLong());
    }

    // Test 3: parcel not found
    @Test
    void testHandle_ParcelNotFound_ShouldReturnError() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertFalse(response.isSuccess());
        assertEquals("Parcel not found", response.getMessage());
        verify(parcelRepository, times(1)).findById(1L);
    }

    // Test 4: new name already exists
    @Test
    void testHandle_ParcelNameAlreadyExists_ShouldReturnError() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.existsByName(dto.getName())).thenReturn(true);

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertFalse(response.isSuccess());
        assertEquals("Parcel name already exists", response.getMessage());
        verify(parcelRepository, never()).save(any());
    }

    // Test 5: warehouse not found
    @Test
    void testHandle_WarehouseNotFound_ShouldReturnError() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Warehouse not found"));
        verify(parcelRepository, never()).save(any());
    }

    // Test 6: valid update (happy path)
    @Test
    void testHandle_ValidUpdate_ShouldReturnSuccess() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(parcelMapper.toDto(any(ParcelDao.class))).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertTrue(response.isSuccess());
        assertEquals(dto.getName(), response.getData().getName());
        verify(parcelRepository, times(1)).save(any(ParcelDao.class));
    }
}