package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetNextDayParcelScheduleHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ParcelMapperHandler parcelMapper;

    @InjectMocks
    private GetNextDayParcelScheduleHandler handler;

    private ParcelDao parcelPending1;
    private ParcelDao parcelPending2;
    private ParcelDao parcelDelivered;
    private WareHouseDao warehouseA;
    private WareHouseDao warehouseB;

    private ParcelResponseDto dtoA;
    private ParcelResponseDto dtoB;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        warehouseA = new WareHouseDao();
        warehouseA.setId(1L);
        warehouseA.setName("Main Warehouse");

        warehouseB = new WareHouseDao();
        warehouseB.setId(2L);
        warehouseB.setName("Secondary Warehouse");

        parcelPending1 = new ParcelDao();
        parcelPending1.setId(101L);
        parcelPending1.setName("Parcel A");
        parcelPending1.setStatus("pending");
        parcelPending1.setWarehouse(warehouseA);
        parcelPending1.setCreatedAt(ZonedDateTime.now());

        parcelPending2 = new ParcelDao();
        parcelPending2.setId(102L);
        parcelPending2.setName("Parcel B");
        parcelPending2.setStatus("pending");
        parcelPending2.setWarehouse(warehouseB);
        parcelPending2.setCreatedAt(ZonedDateTime.now());

        parcelDelivered = new ParcelDao();
        parcelDelivered.setId(103L);
        parcelDelivered.setName("Parcel C");
        parcelDelivered.setStatus("delivered");
        parcelDelivered.setWarehouse(warehouseA);
        parcelDelivered.setCreatedAt(ZonedDateTime.now());

        dtoA = new ParcelResponseDto();
        dtoA.setName("Parcel A");

        dtoB = new ParcelResponseDto();
        dtoB.setName("Parcel B");
    }

    // Test 1: No parcels at all
    @Test
    void testHandle_NoParcels_ShouldReturnError() {
        when(parcelRepository.findAll()).thenReturn(Collections.emptyList());

        ApiResponse<Map<String, List<ParcelResponseDto>>> response = handler.Handle();

        assertFalse(response.isSuccess());
        assertEquals("No pending parcels scheduled for next day", response.getMessage());
    }

    // Test 2: No pending parcels (all non-pending)
    @Test
    void testHandle_NoPendingParcels_ShouldReturnError() {
        when(parcelRepository.findAll()).thenReturn(List.of(parcelDelivered));

        ApiResponse<Map<String, List<ParcelResponseDto>>> response = handler.Handle();

        assertFalse(response.isSuccess());
        assertEquals("No pending parcels scheduled for next day", response.getMessage());
    }

    // Test 3: Pending parcel with null warehouse (should be skipped)
    @Test
    void testHandle_PendingParcelWithNullWarehouse_ShouldSkip() {
        parcelPending1.setWarehouse(null);
        when(parcelRepository.findAll()).thenReturn(List.of(parcelPending1));
        when(parcelMapper.toDto(any())).thenReturn(dtoA);

        ApiResponse<Map<String, List<ParcelResponseDto>>> response = handler.Handle();

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty(), "Parcels with null warehouse should not be grouped");
    }

    // Test 4: Valid pending parcels grouped correctly by warehouse
    @Test
    void testHandle_ValidPendingParcels_ShouldGroupByWarehouse() {
        when(parcelRepository.findAll()).thenReturn(List.of(parcelPending1, parcelPending2, parcelDelivered));
        when(parcelMapper.toDto(parcelPending1)).thenReturn(dtoA);
        when(parcelMapper.toDto(parcelPending2)).thenReturn(dtoB);

        ApiResponse<Map<String, List<ParcelResponseDto>>> response = handler.Handle();

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());

        String keyA = "1 - Main Warehouse";
        String keyB = "2 - Secondary Warehouse";

        assertTrue(response.getData().containsKey(keyA));
        assertTrue(response.getData().containsKey(keyB));

        assertEquals("Parcel A", response.getData().get(keyA).get(0).getName());
        assertEquals("Parcel B", response.getData().get(keyB).get(0).getName());

        verify(parcelMapper, times(2)).toDto(any());
    }
}