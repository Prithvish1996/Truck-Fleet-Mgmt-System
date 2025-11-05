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
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListWarehousesTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private WarehouseMapperHandler mapper;

    @InjectMocks
    private ListWarehousesHandler handler;

    private WareHouseDao warehouse;
    private WareHouseResponseDto dto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        warehouse = new WareHouseDao();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");

        dto = new WareHouseResponseDto();
        dto.setId(1L);
        dto.setName("Main Warehouse");
    }

    /**
     * Test 1: Empty page (no warehouses)
     */
    @Test
    void handle_EmptyPage_ShouldReturnEmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WareHouseDao> emptyPage = new PageImpl<>(List.of());

        when(warehouseRepository.findByActiveTrue(pageable)).thenReturn(emptyPage);

        ApiResponse<Map<String, Object>> response = handler.Handle(pageable);

        assertTrue(response.isSuccess());
        Map<String, Object> data = response.getData();
        assertEquals(0L, data.get("totalItems"));
        assertTrue(((List<?>) data.get("data")).isEmpty());
        verify(warehouseRepository).findByActiveTrue(pageable);
    }

    /**
     * Test 2: Non-empty page with one warehouse
     */
    @Test
    void handle_OneWarehouse_ShouldMapAndReturnSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WareHouseDao> page = new PageImpl<>(List.of(warehouse), pageable, 1);

        when(warehouseRepository.findByActiveTrue(pageable)).thenReturn(page);
        when(mapper.toDto(warehouse, 0L, 0L, 0L)).thenReturn(dto);
        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.PENDING)).thenReturn(2L);
        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.SCHEDULED)).thenReturn(1L);
        when(parcelRepository.countByWarehouseAndStatus(warehouse, StatusEnum.DELIVERED)).thenReturn(3L);

        ApiResponse<Map<String, Object>> response = handler.Handle(pageable);

        assertTrue(response.isSuccess());
        Map<String, Object> data = response.getData();
        assertEquals(1L, data.get("totalItems"));
        assertEquals(1, ((List<?>) data.get("data")).size());
        verify(mapper).toDto(warehouse, 0L, 0L, 0L);
        verify(parcelRepository, times(3)).countByWarehouseAndStatus(eq(warehouse), any());
    }

    /**
     * Test 3: Multiple warehouses (MC/DC coverage for loop)
     */
    @Test
    void handle_MultipleWarehouses_ShouldProcessEach() {
        WareHouseDao w2 = new WareHouseDao();
        w2.setId(2L);
        w2.setName("Backup Warehouse");

        WareHouseResponseDto dto2 = new WareHouseResponseDto();
        dto2.setId(2L);
        dto2.setName("Backup Warehouse");

        Pageable pageable = PageRequest.of(0, 10);
        Page<WareHouseDao> page = new PageImpl<>(List.of(warehouse, w2));

        when(warehouseRepository.findByActiveTrue(pageable)).thenReturn(page);
        when(mapper.toDto(any(), eq(0L), eq(0L), eq(0L)))
                .thenReturn(dto, dto2);
        when(parcelRepository.countByWarehouseAndStatus(any(), any())).thenReturn(1L);

        ApiResponse<Map<String, Object>> response = handler.Handle(pageable);

        assertTrue(response.isSuccess());
        List<?> list = (List<?>) response.getData().get("data");
        assertEquals(2, list.size());
        verify(mapper, times(2)).toDto(any(), eq(0L), eq(0L), eq(0L));
    }
}