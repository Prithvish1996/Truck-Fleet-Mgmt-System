package com.saxion.proj.tfms.planner.services.parcelServices;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.LocationRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.LocationRepository;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.Optional;

public class CreateParcelHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ParcelMapperHandler parcelMapper;

    @InjectMocks
    private CreateParcelHandler createParcelHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // TC1: Parcel name exists
    @Test
    void handle_ShouldReturnError_WhenParcelNameExists() {
        ParcelRequestDto dto = new ParcelRequestDto();
        dto.setName("Existing Parcel");

        when(parcelRepository.existsByName(dto.getName())).thenReturn(true);

        ApiResponse<ParcelResponseDto> response = createParcelHandler.Handle(dto);

        assertFalse(response.isSuccess());
        assertEquals("Parcel name already exists", response.getMessage());
        verify(parcelRepository, never()).save(any());
    }

    // TC2: Warehouse does not exist -> should create warehouse
    @Test
    void handle_ShouldCreateWarehouse_WhenWarehouseDoesNotExist() {
        ParcelRequestDto dto = new ParcelRequestDto();
        dto.setName("New Parcel");

        WareHouseRequestDto warehouseDto = new WareHouseRequestDto();
        warehouseDto.setName("New Warehouse");

        LocationRequestDto warehouseLocationDto = new LocationRequestDto();
        warehouseLocationDto.setAddress("Warehouse Address");
        warehouseLocationDto.setCity("City");
        warehouseLocationDto.setPostcode("12345");
        warehouseLocationDto.setLatitude(10.0);
        warehouseLocationDto.setLongitude(20.0);

        warehouseDto.setLocation(warehouseLocationDto);
        dto.setWarehouse(warehouseDto);

        LocationRequestDto deliveryLocationDto = new LocationRequestDto();
        deliveryLocationDto.setAddress("Delivery Address");
        deliveryLocationDto.setCity("City");
        deliveryLocationDto.setPostcode("54321");
        deliveryLocationDto.setLatitude(11.0);
        deliveryLocationDto.setLongitude(21.0);

        dto.setDeliveryLocation(deliveryLocationDto);

        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);
        when(warehouseRepository.findByName("New Warehouse")).thenReturn(Optional.empty());

        // Mock location repository for warehouse location
        LocationDao warehouseLocation = new LocationDao();
        warehouseLocation.setId(1L);
        when(locationRepository.findByPostalCode("12345")).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationDao.class))).thenReturn(warehouseLocation);

        // Mock location repository for delivery location
        LocationDao deliveryLocation = new LocationDao();
        deliveryLocation.setId(2L);
        when(locationRepository.findByPostalCode("54321")).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationDao.class))).thenReturn(deliveryLocation);

        WareHouseDao savedWarehouse = new WareHouseDao();
        savedWarehouse.setId(1L);
        savedWarehouse.setName("New Warehouse");

        LocationDao saveWarehouseLocationDto = new LocationDao();
        saveWarehouseLocationDto.setAddress("Warehouse Address");
        saveWarehouseLocationDto.setCity("City");
        saveWarehouseLocationDto.setPostalCode("11111");
        saveWarehouseLocationDto.setLatitude(10.0);
        saveWarehouseLocationDto.setLongitude(20.0);
        savedWarehouse.setLocation(saveWarehouseLocationDto);
        when(warehouseRepository.save(any(WareHouseDao.class))).thenReturn(savedWarehouse);

        ParcelResponseDto responseDto = new ParcelResponseDto();
        responseDto.setName(dto.getName());
        when(parcelMapper.toDto(any())).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = createParcelHandler.Handle(dto);

        assertTrue(response.isSuccess());
        assertEquals("New Parcel", response.getData().getName());

        verify(parcelRepository).save(any());
        verify(warehouseRepository).save(any());
        verify(locationRepository, times(2)).save(any());
    }

    // TC3: Warehouse exists -> should reuse warehouse
    @Test
    void handle_ShouldReuseWarehouse_WhenWarehouseExists() {
        ParcelRequestDto dto = new ParcelRequestDto();
        dto.setName("New Parcel");

        WareHouseRequestDto warehouseDto = new WareHouseRequestDto();
        warehouseDto.setName("Existing Warehouse");

        LocationRequestDto deliveryLocationDto = new LocationRequestDto();
        deliveryLocationDto.setAddress("Delivery Address");
        deliveryLocationDto.setCity("City");
        deliveryLocationDto.setPostcode("54321");
        deliveryLocationDto.setLatitude(11.0);
        deliveryLocationDto.setLongitude(21.0);

        dto.setWarehouse(warehouseDto);
        dto.setDeliveryLocation(deliveryLocationDto);

        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);

        WareHouseDao existingWarehouse = new WareHouseDao();
        existingWarehouse.setId(1L);
        existingWarehouse.setName("Existing Warehouse");

        LocationDao saveExistingLocationDto = new LocationDao();
        saveExistingLocationDto.setAddress("Warehouse Address");
        saveExistingLocationDto.setCity("City");
        saveExistingLocationDto.setPostalCode("11111");
        saveExistingLocationDto.setLatitude(10.0);
        saveExistingLocationDto.setLongitude(20.0);
        existingWarehouse.setLocation(saveExistingLocationDto);

        when(warehouseRepository.findByName("Existing Warehouse")).thenReturn(Optional.of(existingWarehouse));

        LocationDao deliveryLocation = new LocationDao();
        deliveryLocation.setId(2L);
        when(locationRepository.findByPostalCode("54321")).thenReturn(Optional.empty());
        when(locationRepository.save(any(LocationDao.class))).thenReturn(deliveryLocation);

        ParcelResponseDto responseDto = new ParcelResponseDto();
        responseDto.setName(dto.getName());
        when(parcelMapper.toDto(any())).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = createParcelHandler.Handle(dto);

        assertTrue(response.isSuccess());
        assertEquals("New Parcel", response.getData().getName());

        verify(parcelRepository).save(any());
        verify(warehouseRepository, never()).save(any());
        verify(locationRepository).save(any());
    }

    // TC4: Delivery location already exists -> should reuse existing location
    @Test
    void handle_ShouldReuseExistingLocation_WhenPostcodeExists() {
        ParcelRequestDto dto = new ParcelRequestDto();
        dto.setName("Parcel With Existing Location");

        WareHouseRequestDto warehouseDto = new WareHouseRequestDto();
        warehouseDto.setName("Existing Warehouse");
        LocationRequestDto warehouseLocationDto = new LocationRequestDto();
        warehouseLocationDto.setAddress("Warehouse Address");
        warehouseLocationDto.setCity("City");
        warehouseLocationDto.setPostcode("11111");
        warehouseLocationDto.setLatitude(10.0);
        warehouseLocationDto.setLongitude(20.0);
        warehouseDto.setLocation(warehouseLocationDto);
        dto.setWarehouse(warehouseDto);

        LocationRequestDto deliveryLocationDto = new LocationRequestDto();
        deliveryLocationDto.setAddress("Delivery Address");
        deliveryLocationDto.setCity("City");
        deliveryLocationDto.setPostcode("22222"); // Existing postcode
        deliveryLocationDto.setLatitude(11.0);
        deliveryLocationDto.setLongitude(21.0);
        dto.setDeliveryLocation(deliveryLocationDto);

        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);

        // Mock existing warehouse
        WareHouseDao existingWarehouse = new WareHouseDao();
        existingWarehouse.setId(1L);
        existingWarehouse.setName("Existing Warehouse");
        when(warehouseRepository.findByName("Existing Warehouse")).thenReturn(Optional.of(existingWarehouse));

        // Mock existing delivery location
        LocationDao existingDeliveryLocation = new LocationDao();
        existingDeliveryLocation.setId(2L);
        existingDeliveryLocation.setPostalCode("22222");
        when(locationRepository.findByPostalCode("22222")).thenReturn(Optional.of(existingDeliveryLocation));

        ParcelResponseDto responseDto = new ParcelResponseDto();
        responseDto.setName(dto.getName());
        when(parcelMapper.toDto(any())).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = createParcelHandler.Handle(dto);

        assertTrue(response.isSuccess());
        assertEquals("Parcel With Existing Location", response.getData().getName());

        verify(parcelRepository).save(any());
        verify(warehouseRepository, never()).save(any()); // Warehouse exists
        verify(locationRepository, never()).save(argThat(loc -> "22222".equals(loc.getPostalCode()))); // Location reused, not saved
    }

}