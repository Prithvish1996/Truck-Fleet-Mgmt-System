package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.*;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import com.saxion.proj.tfms.planner.repository.DriverTruckAssignmentRepository;
import com.saxion.proj.tfms.planner.repository.TruckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignDriverToTruckTest {

    @Mock private DriverRepository driverRepository;
    @Mock private TruckRepository truckRepository;
    @Mock private DriverTruckAssignmentRepository assignmentRepository;
    @InjectMocks private AssignDriverToTruckHandler handler;

    private DriverDao driver;
    private TruckDao truck;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        driver = new DriverDao();
        truck = new TruckDao();
        UserDao user = new UserDao();
        user.setEmail("test@example.com");
        user.setUsername("John");
        driver.setUser(user);
    }

    @Test
    void handle_InvalidIds_ShouldReturnError() {
        ApiResponse<String> response = handler.Handle(null, 2L);
        assertFalse(response.isSuccess());
        assertEquals("Invalid driver or truck ID", response.getMessage());
    }

    @Test
    void handle_DriverNotFound_ShouldReturnError() {
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());
        ApiResponse<String> response = handler.Handle(1L, 2L);
        assertFalse(response.isSuccess());
        assertEquals("Driver not found", response.getMessage());
    }

    @Test
    void handle_TruckNotFound_ShouldReturnError() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(truckRepository.findById(2L)).thenReturn(Optional.empty());

        ApiResponse<String> response = handler.Handle(1L, 2L);
        assertFalse(response.isSuccess());
        assertEquals("Truck not found", response.getMessage());
    }

    @Test
    void handle_DriverAlreadyAssigned_ShouldReturnError() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(truckRepository.findById(2L)).thenReturn(Optional.of(truck));
        when(assignmentRepository.existsByDriverAndDateAssignedBetween(any(), any(), any()))
                .thenReturn(true);

        ApiResponse<String> response = handler.Handle(1L, 2L);
        assertFalse(response.isSuccess());
        assertEquals("Driver already has a truck assignment for today", response.getMessage());
    }

    @Test
    void handle_ValidAssignment_ShouldSucceed() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(truckRepository.findById(2L)).thenReturn(Optional.of(truck));
        when(assignmentRepository.existsByDriverAndDateAssignedBetween(any(), any(), any()))
                .thenReturn(false);

        ApiResponse<String> response = handler.Handle(1L, 2L);

        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        verify(assignmentRepository).save(any(DriverTruckAssignmentDao.class));
    }
}

