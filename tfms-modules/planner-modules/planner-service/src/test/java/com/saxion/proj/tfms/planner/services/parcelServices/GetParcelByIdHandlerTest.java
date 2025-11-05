package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class GetParcelByIdHandlerTest {

    private GetParcelByIdHandler handler;
    private ParcelRepository parcelRepository;
    private ParcelMapperHandler parcelMapper;

    @BeforeEach
    void setUp() {
        parcelRepository = mock(ParcelRepository.class);
        parcelMapper = mock(ParcelMapperHandler.class);
        handler = new GetParcelByIdHandler(parcelRepository, parcelMapper);
    }

    @Test
    void handle_ParcelIdIsNull() {
        ApiResponse<ParcelResponseDto> response = handler.Handle(null);
        assertFalse(response.isSuccess());
        assertEquals("Invalid parcel ID", response.getMessage());
    }

    @Test
    void handle_ParcelIdIsZeroOrNegative() {
        ApiResponse<ParcelResponseDto> responseZero = handler.Handle(0L);
        assertFalse(responseZero.isSuccess());
        assertEquals("Invalid parcel ID", responseZero.getMessage());

        ApiResponse<ParcelResponseDto> responseNegative = handler.Handle(-5L);
        assertFalse(responseNegative.isSuccess());
        assertEquals("Invalid parcel ID", responseNegative.getMessage());
    }

    @Test
    void handle_ParcelNotFound() {
        Long parcelId = 10L;
        when(parcelRepository.findById(parcelId)).thenReturn(Optional.empty());

        ApiResponse<ParcelResponseDto> response = handler.Handle(parcelId);
        assertFalse(response.isSuccess());
        assertEquals("Parcel not found", response.getMessage());
    }

    @Test
    void handle_ParcelFound() {
        Long parcelId = 1L;
        ParcelDao parcelDao = new ParcelDao();
        ParcelResponseDto dto = new ParcelResponseDto();

        when(parcelRepository.findByIdWithRelations(parcelId)).thenReturn(Optional.of(parcelDao));
        when(parcelMapper.toDto(parcelDao)).thenReturn(dto);

        ApiResponse<ParcelResponseDto> response = handler.Handle(parcelId);
        assertTrue(response.isSuccess());
        assertEquals(dto, response.getData());
    }
}