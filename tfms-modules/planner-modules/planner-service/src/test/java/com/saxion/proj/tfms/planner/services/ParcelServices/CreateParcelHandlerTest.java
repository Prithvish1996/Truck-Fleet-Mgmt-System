package com.saxion.proj.tfms.planner.services.ParcelServices;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
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

    // TC2: Warehouse does not exist
    @Test
    void handle_ShouldThrowException_WhenWarehouseNotFound() {
        ParcelRequestDto dto = new ParcelRequestDto();
        dto.setName("New Parcel");
        dto.setWarehouseId(1L);

        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);
        when(warehouseRepository.findById(dto.getWarehouseId())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> createParcelHandler.Handle(dto));
        assertEquals("Warehouse not found with ID: 1", ex.getMessage());
        verify(parcelRepository, never()).save(any());
    }

    // TC3: Successful creation
    @Test
    void handle_ShouldSaveParcel_WhenValidInput() {
        ParcelRequestDto dto = new ParcelRequestDto();
        dto.setName("New Parcel");
        dto.setWarehouseId(1L);

        WareHouseDao warehouse = new WareHouseDao();
        warehouse.setId(1L);

        ParcelResponseDto responseDto = new ParcelResponseDto();
        responseDto.setName(dto.getName());

        when(parcelRepository.existsByName(dto.getName())).thenReturn(false);
        when(warehouseRepository.findById(dto.getWarehouseId())).thenReturn(Optional.of(warehouse));
        when(parcelMapper.toDto(any())).thenReturn(responseDto);

        ApiResponse<ParcelResponseDto> response = createParcelHandler.Handle(dto);

        assertTrue(response.isSuccess());
        assertEquals("New Parcel", response.getData().getName());
        verify(parcelRepository).save(any());
    }
}
