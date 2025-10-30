package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.springframework.data.domain.*;
import java.util.*;
import java.util.stream.Collectors;
import static org.mockito.Mockito.*;

class GetAllParcelsHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapperHandler parcelMapper;

    @InjectMocks
    private GetAllParcelHandler handler;

    private WareHouseDao warehouseA;
    private ParcelDao parcel1;
    private ParcelDao parcel2;

    private ParcelResponseDto dto1;
    private ParcelResponseDto dto2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        warehouseA = new WareHouseDao();
        warehouseA.setId(1L);
        warehouseA.setName("Main Warehouse");

        parcel1 = new ParcelDao();
        parcel1.setId(101L);
        parcel1.setName("Box A");
        //parcel1.setCity("Amsterdam");
        parcel1.setStatus(StatusEnum.PENDING);
        parcel1.setRecipientName("John Doe");
        parcel1.setRecipientPhone("12345");
        parcel1.setWarehouse(warehouseA);

        parcel2 = new ParcelDao();
        parcel2.setId(102L);
        parcel2.setName("Package B");
        //parcel2.setCity("Rotterdam");
        parcel2.setStatus(StatusEnum.PENDING);
        parcel2.setRecipientName("Jane Doe");
        parcel2.setRecipientPhone("67890");
        parcel2.setWarehouse(warehouseA);

        dto1 = new ParcelResponseDto();
        dto1.setName(parcel1.getName());

        dto2 = new ParcelResponseDto();
        dto2.setName(parcel2.getName());
    }

    // Test 1: Null warehouseId
    @Test
    void handle_NullWarehouseId_ShouldReturnError() {
        ApiResponse<Map<String, Object>> response = handler.Handle(null, null, 0, 10);
        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
    }

    // Test 2: Non-positive warehouseId
    @Test
    void handle_NonPositiveWarehouseId_ShouldReturnError() {
        ApiResponse<Map<String, Object>> response = handler.Handle(0L, null, 0, 10);
        assertFalse(response.isSuccess());
        assertEquals("Invalid warehouse ID", response.getMessage());
    }

    // Test 3: Invalid pagination (negative page or size <= 0)
    @Test
    void handle_InvalidPagination_ShouldReturnError() {
        ApiResponse<Map<String, Object>> response1 = handler.Handle(1L, null, -1, 10);
        ApiResponse<Map<String, Object>> response2 = handler.Handle(1L, null, 0, 0);

        assertFalse(response1.isSuccess());
        assertEquals("Invalid pagination parameters", response1.getMessage());

        assertFalse(response2.isSuccess());
        assertEquals("Invalid pagination parameters", response2.getMessage());
    }

    // Test 4: No parcels found for the warehouse
    @Test
    void handle_ValidWarehouseId_NoParcels_ShouldReturnEmptyList() {
        Page<ParcelDao> emptyPage = new PageImpl<>(Collections.emptyList());
        when(parcelRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        ApiResponse<Map<String, Object>> response = handler.Handle(1L, null, 0, 10);

        assertTrue(response.isSuccess()); // success but empty list
        assertTrue(((List<?>) response.getData().get("data")).isEmpty());
    }

    // Test 5: Valid warehouse, filter = null (should return all)
    @Test
    void handle_ValidWarehouseId_NoFilter_ShouldReturnAllParcels() {
        // Mock repo IDs
        when(parcelRepository.findParcelIds(any(Pageable.class)))
                .thenReturn(List.of(1L, 2L));

        // Mock full parcel fetch
        when(parcelRepository.findAllWithLocations(List.of(1L, 2L)))
                .thenReturn(List.of(parcel1, parcel2));

        // Mock mapper
        when(parcelMapper.toDto(parcel1)).thenReturn(dto1);
        when(parcelMapper.toDto(parcel2)).thenReturn(dto2);

        // Mock count for pagination metadata
        when(parcelRepository.count()).thenReturn(2L);

        ApiResponse<Map<String, Object>> response = handler.Handle(1L, null, 0, 10);

        assertTrue(response.isSuccess());
        List<?> items = (List<?>) response.getData().get("data");
        assertEquals(2, items.size());
        verify(parcelMapper, times(2)).toDto(any());
    }

    // Test 6: Valid warehouse + filter (match on name)
    @Test
    void handle_FilterByParcelName_ShouldReturnFilteredResult() {
        // Mock repo IDs
        when(parcelRepository.findParcelIds(any(Pageable.class)))
                .thenReturn(List.of(1L, 2L));

        // Mock full parcel fetch
        when(parcelRepository.findAllWithLocations(List.of(1L, 2L)))
                .thenReturn(List.of(parcel1, parcel2));

        // Mock mapper
        when(parcelMapper.toDto(parcel1)).thenReturn(dto1);
        when(parcelMapper.toDto(parcel2)).thenReturn(dto2);

        // Mock count
        when(parcelRepository.count()).thenReturn(2L);

        ApiResponse<Map<String, Object>> response = handler.Handle(1L, "Box", 0, 10);

        assertTrue(response.isSuccess());
        List<ParcelResponseDto> items = ((List<?>) response.getData().get("data"))
                .stream().map(o -> (ParcelResponseDto) o).collect(Collectors.toList());
        assertEquals(1, items.size());
        assertEquals("Box A", items.get(0).getName());
    }

    // Test 8: Filter text doesn’t match anything
    @Test
    void handle_FilterTextNoMatch_ShouldReturnEmpty() {
        Page<ParcelDao> page = new PageImpl<>(List.of(parcel1, parcel2));
        when(parcelRepository.findAll(any(Pageable.class))).thenReturn(page);

        ApiResponse<Map<String, Object>> response = handler.Handle(1L, "XYZ", 0, 10);

        assertTrue(response.isSuccess());
        assertTrue(((List<?>) response.getData().get("data")).isEmpty());
    }
}