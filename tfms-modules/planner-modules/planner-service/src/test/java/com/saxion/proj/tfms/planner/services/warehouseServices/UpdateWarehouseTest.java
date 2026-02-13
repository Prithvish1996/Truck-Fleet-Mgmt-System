package com.saxion.proj.tfms.planner.services.warehouseServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.LocationRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
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

class UpdateWarehouseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private WarehouseMapperHandler mapper;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private UpdateWarehouseHandler handler;

    private WareHouseDao warehouse;
    private WareHouseRequestDto requestDto;
    private LocationRequestDto locDto;
    private LocationDao location;
    private WareHouseResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        warehouse = new WareHouseDao();
        warehouse.setId(1L);
        warehouse.setName("Old Warehouse");

        locDto = new LocationRequestDto();
        locDto.setAddress("123 Street");
        locDto.setCity("TestCity");
        locDto.setLatitude(10.0);
        locDto.setLongitude(20.0);
        locDto.setPostcode("12345");

        requestDto = new WareHouseRequestDto();
        requestDto.setName("Updated Warehouse");
        requestDto.setLocation(locDto);

        location = new LocationDao();
        location.setId(1L);
        location.setCity("TestCity");

        responseDto = new WareHouseResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Updated Warehouse");
    }

    /**
     * Test 1: warehouseId == null
     */
    @Test
    void handle_NullWarehouseId_ShouldReturnInvalidIdError() {
        ApiResponse<WareHouseResponseDto> response = handler.Handle(null, requestDto);

        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
        verifyNoInteractions(warehouseRepository, parcelRepository, mapper, locationRepository);
    }

    /**
     * Test 2: warehouseId <= 0
     */
    @Test
    void handle_NegativeWarehouseId_ShouldReturnInvalidIdError() {
        ApiResponse<WareHouseResponseDto> response = handler.Handle(0L, requestDto);

        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
        verifyNoInteractions(warehouseRepository, parcelRepository, mapper, locationRepository);
    }

    /**
     * Test 3: Warehouse not found
     */
    @Test
    void handle_WarehouseNotFound_ShouldReturnError() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<WareHouseResponseDto> response = handler.Handle(1L, requestDto);

        assertFalse(response.isSuccess());
        assertEquals("Warehouse not found", response.getMessage());
        verify(warehouseRepository).findById(1L);
        verifyNoMoreInteractions(warehouseRepository);
    }

    /**
     * Test 4: Warehouse found, location exists (no new creation)
     */
    @Test
    void handle_WarehouseFound_ExistingLocation_ShouldUpdateSuccessfully() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(locationRepository.findByPostalCode("12345")).thenReturn(Optional.of(location));
        when(mapper.toDto(warehouse, 0L, 0L, 0L)).thenReturn(responseDto);

        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.PENDING)).thenReturn(2L);
        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.SCHEDULED)).thenReturn(1L);
        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.DELIVERED)).thenReturn(3L);

        ApiResponse<WareHouseResponseDto> response = handler.Handle(1L, requestDto);

        assertTrue(response.isSuccess());
        assertEquals("Updated Warehouse", response.getData().getName());
        verify(locationRepository, never()).save(any());
        verify(warehouseRepository).save(warehouse);
    }

    /**
     * Test 5: Warehouse found, location not found → create new location
     */
    @Test
    void handle_WarehouseFound_NewLocation_ShouldSaveNewLocation() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(locationRepository.findByPostalCode("12345")).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationDao.class))).thenReturn(location);
        when(mapper.toDto(warehouse, 0L, 0L, 0L)).thenReturn(responseDto);

        ApiResponse<WareHouseResponseDto> response = handler.Handle(1L, requestDto);

        assertTrue(response.isSuccess());
        assertEquals("Updated Warehouse", response.getData().getName());
        verify(locationRepository).save(any(LocationDao.class));
        verify(warehouseRepository).save(warehouse);
    }
}