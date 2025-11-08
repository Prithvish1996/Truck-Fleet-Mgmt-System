package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.LocationRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateParcelHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private LocationRepository locationRepository;

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

        warehouse = new WareHouseDao();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");

        parcel = new ParcelDao();
        parcel.setId(1L);
        parcel.setName("Old Parcel");
        parcel.setWarehouse(warehouse);

        // Setup DTO
        dto = new ParcelRequestDto();
        dto.setName("Updated Parcel");
        dto.setWeight(5.0);
        dto.setVolume(2.0);
        dto.setRecipientName("John Doe");
        dto.setRecipientPhone("+31 6 1111 2222");
        dto.setDeliveryInstructions("Leave at door");

        // Warehouse DTO
        WareHouseRequestDto warehouseDto = new WareHouseRequestDto();
        warehouseDto.setName("Main Warehouse");

        LocationRequestDto warehouseLocationDto = new LocationRequestDto();
        warehouseLocationDto.setAddress("Warehouse Address");
        warehouseLocationDto.setCity("City");
        warehouseLocationDto.setPostcode("11111");
        warehouseLocationDto.setLatitude(10.0);
        warehouseLocationDto.setLongitude(20.0);
        warehouseDto.setLocation(warehouseLocationDto);

        dto.setWarehouse(warehouseDto);

        // Delivery location DTO
        LocationRequestDto deliveryLocationDto = new LocationRequestDto();
        deliveryLocationDto.setAddress("Delivery Address");
        deliveryLocationDto.setCity("City");
        deliveryLocationDto.setPostcode("22222");
        deliveryLocationDto.setLatitude(11.0);
        deliveryLocationDto.setLongitude(21.0);
        dto.setDeliveryLocation(deliveryLocationDto);

        responseDto = new ParcelResponseDto();
        responseDto.setName(dto.getName());
    }

    // Test 1: parcelId == null
    @Test
    void handle_NullParcelId_ShouldReturnError() {
        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(null, dto);
        assertFalse(response.isSuccess());
        assertEquals("Invalid parcel ID", response.getMessage());
        verify(parcelRepository, never()).findById(anyLong());
    }

    // Test 2: parcelId <= 0
    @Test
    void handle_InvalidParcelId_ShouldReturnError() {
        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(0L, dto);
        assertFalse(response.isSuccess());
        assertEquals("Invalid parcel ID", response.getMessage());
        verify(parcelRepository, never()).findById(anyLong());
    }

    // Test 3: parcel not found
    @Test
    void handle_ParcelNotFound_ShouldReturnError() {
        when(parcelRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());
        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);
        assertFalse(response.isSuccess());
        assertEquals("Parcel not found", response.getMessage());
        verify(parcelRepository, times(1)).findByIdWithRelations(1L);
    }

    // Test 4: parcel name already exists
    @Test
    void handle_ParcelNameAlreadyExists_ShouldReturnError() {
        when(parcelRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.existsByName(dto.getName())).thenReturn(true);

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertFalse(response.isSuccess());
        assertEquals("Parcel name already exists", response.getMessage());
        verify(parcelRepository, never()).save(any());
    }

    // Test 5: warehouse does not exist -> should create warehouse
    @Test
    void handle_houldCreateWarehouse_WhenWarehouseNotFound() {
        when(parcelRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);
        when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.empty());

        // Mock warehouse location
        LocationDao warehouseLocation = new LocationDao();
        warehouseLocation.setId(1L);
        when(locationRepository.findByPostalCode("11111")).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationDao.class))).thenReturn(warehouseLocation);

        // Mock delivery location
        LocationDao deliveryLocation = new LocationDao();
        deliveryLocation.setId(2L);
        when(locationRepository.findByPostalCode("22222")).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationDao.class))).thenReturn(deliveryLocation);

        WareHouseDao savedWarehouse = new WareHouseDao();
        savedWarehouse.setId(1L);
        savedWarehouse.setName("Main Warehouse");

        LocationDao saveWarehouseLocationDto = new LocationDao();
        saveWarehouseLocationDto.setAddress("Warehouse Address");
        saveWarehouseLocationDto.setCity("City");
        saveWarehouseLocationDto.setPostalCode("11111");
        saveWarehouseLocationDto.setLatitude(10.0);
        saveWarehouseLocationDto.setLongitude(20.0);
        savedWarehouse.setLocation(saveWarehouseLocationDto);
        when(warehouseRepository.save(any(WareHouseDao.class))).thenReturn(savedWarehouse);

        when(parcelMapper.toDto(any())).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertTrue(response.isSuccess());
        assertEquals(dto.getName(), response.getData().getName());

        verify(parcelRepository).save(any());
        verify(warehouseRepository).save(any());
        verify(locationRepository, times(2)).save(any());
    }

    // Test 6: warehouse exists -> reuse warehouse
    @Test
    void handle_ShouldReuseWarehouse_WhenWarehouseExists() {
        when(parcelRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);

        WareHouseDao existingWarehouse = new WareHouseDao();
        existingWarehouse.setId(1L);
        existingWarehouse.setName("Main Warehouse");
        when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(existingWarehouse));

        // Mock delivery location
        LocationDao deliveryLocation = new LocationDao();
        deliveryLocation.setId(2L);
        when(locationRepository.findByPostalCode("22222")).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationDao.class))).thenReturn(deliveryLocation);

        when(parcelMapper.toDto(any())).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertTrue(response.isSuccess());
        assertEquals(dto.getName(), response.getData().getName());

        verify(parcelRepository).save(any());
        verify(warehouseRepository, never()).save(any());
        verify(locationRepository).save(any());
    }

    // Test 7: delivery location exists -> reuse existing location
    @Test
    void handle_ShouldReuseExistingDeliveryLocation_WhenPostcodeExists() {
        when(parcelRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);

        WareHouseDao existingWarehouse = new WareHouseDao();
        existingWarehouse.setId(1L);
        existingWarehouse.setName("Main Warehouse");
        when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(existingWarehouse));

        // Existing delivery location
        LocationDao existingDeliveryLocation = new LocationDao();
        existingDeliveryLocation.setId(2L);
        existingDeliveryLocation.setPostalCode("22222");
        when(locationRepository.findByPostalCode("22222")).thenReturn(Optional.of(existingDeliveryLocation));

        when(parcelMapper.toDto(any())).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = updateParcelHandler.Handle(1L, dto);

        assertTrue(response.isSuccess());
        assertEquals(dto.getName(), response.getData().getName());

        verify(parcelRepository).save(any());
        verify(locationRepository, never()).save(argThat(loc -> "22222".equals(loc.getPostalCode())));
        verify(warehouseRepository, never()).save(any());
    }
}