package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteParcelHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private DeleteParcelHandler deleteParcelHandler;

    private ParcelDao parcel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parcel = new ParcelDao();
        parcel.setId(1L);
        parcel.setName("Parcel 1");
    }

    // Test 1: parcelId is null -> should return "Invalid parcel ID"
    @Test
    void handle_NullParcelId_ShouldReturnError() {
        ApiResponse<Void> response = deleteParcelHandler.Handle(null);

        assertFalse(response.isSuccess());
        assertEquals("Invalid parcel ID", response.getMessage());
        verify(parcelRepository, never()).findById(anyLong());
        verify(parcelRepository, never()).delete(any());
    }

    // Test 2: parcelId <= 0 -> should return "Invalid parcel ID"
    @Test
    void handle_InvalidParcelId_ShouldReturnError() {
        ApiResponse<Void> response = deleteParcelHandler.Handle(0L);

        assertFalse(response.isSuccess());
        assertEquals("Invalid parcel ID", response.getMessage());
        verify(parcelRepository, never()).findById(anyLong());
        verify(parcelRepository, never()).delete(any());
    }

    // Test 3: parcel not found -> should return "Parcel not found"
    @Test
    void handle_ParcelNotFound_ShouldReturnError() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<Void> response = deleteParcelHandler.Handle(1L);

        assertFalse(response.isSuccess());
        assertEquals("Parcel not found", response.getMessage());
        verify(parcelRepository, times(1)).findById(1L);
        verify(parcelRepository, never()).delete(any());
    }

    // Test 4: parcel exists -> should delete successfully
    @Test
    void handle_ParcelExists_ShouldDeleteSuccessfully() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        ApiResponse<Void> response = deleteParcelHandler.Handle(1L);

        assertTrue(response.isSuccess());
        assertEquals("Parcel deleted successfully", response.getMessage());
        verify(parcelRepository, times(1)).findById(1L);
        verify(parcelRepository, times(1)).delete(parcel);
    }
}