package com.saxion.proj.tfms.planner.services.truckServices;

import com.saxion.proj.tfms.commons.constants.TruckType;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverTruckAssignmentDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import com.saxion.proj.tfms.commons.model.TruckDao;
import com.saxion.proj.tfms.planner.dto.TruckResponseDto;
import com.saxion.proj.tfms.planner.repository.TruckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class GetTruckByIdHandlerTest {

    @Mock
    private TruckRepository truckRepository;

    @InjectMocks
    private GetTruckByIdHandler handler;

    private TruckDao truck;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        truck = new TruckDao();
        truck.setId(1L);
        truck.setPlateNumber("ABC-123");
        truck.setType(TruckType.MEDIUM);
        truck.setMake("Volvo");
        truck.setLastServiceDate(LocalDate.of(2025, 5, 10));
        truck.setLastServicedBy("Mike");
        truck.setVolume(25000.0);
        truck.setIsAvailable(true);
        truck.setRoutes(List.of(new RouteDao()));
        truck.setAssignments(List.of(new DriverTruckAssignmentDao()));
    }

    // --- MC/DC 1: truckId is null ---
    @Test
    void handle_shouldReturnErrorWhenTruckIdIsNull() {
        ApiResponse<TruckResponseDto> response = handler.Handle(null);

        assertFalse(response.isSuccess());
        assertEquals("Invalid truck ID", response.getMessage());
        verifyNoInteractions(truckRepository);
    }

    // --- MC/DC 2: truckId <= 0 ---
    @Test
    void handle_shouldReturnErrorWhenTruckIdIsZeroOrNegative() {
        ApiResponse<TruckResponseDto> response = handler.Handle(0L);

        assertFalse(response.isSuccess());
        assertEquals("Invalid truck ID", response.getMessage());
        verifyNoInteractions(truckRepository);
    }

    // --- MC/DC 3: truck not found in repository ---
    @Test
    void handle_shouldReturnErrorWhenTruckNotFound() {
        when(truckRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<TruckResponseDto> response = handler.Handle(1L);

        assertFalse(response.isSuccess());
        assertEquals("Truck not found", response.getMessage());
        verify(truckRepository).findById(1L);
    }

    // --- MC/DC 4: valid truck found ---
    @Test
    void handle_shouldReturnSuccessWhenTruckExists() {
        when(truckRepository.findById(1L)).thenReturn(Optional.of(truck));

        ApiResponse<TruckResponseDto> response = handler.Handle(1L);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("ABC-123", response.getData().getPlateNumber());
        assertEquals("Volvo", response.getData().getMake());
        assertEquals(1, response.getData().getNumberOfRoutes());
        assertEquals(1, response.getData().getNumberOfAssignment());
        verify(truckRepository).findById(1L);
    }

    // --- MC/DC 5: truck has null routes and assignments ---
    @Test
    void handle_shouldHandleNullRoutesOrAssignmentsGracefully() {
        truck.setRoutes(null);
        truck.setAssignments(null);
        when(truckRepository.findById(1L)).thenReturn(Optional.of(truck));

        ApiResponse<TruckResponseDto> response = handler.Handle(1L);

        assertTrue(response.isSuccess());
        assertEquals(0, response.getData().getNumberOfRoutes());
        assertEquals(0, response.getData().getNumberOfAssignment());
        verify(truckRepository).findById(1L);
    }
}