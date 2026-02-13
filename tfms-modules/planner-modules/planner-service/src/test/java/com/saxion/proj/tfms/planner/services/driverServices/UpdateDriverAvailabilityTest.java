package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.*;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import com.saxion.proj.tfms.planner.repository.DriverTruckAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateDriverAvailabilityTest {

    @Mock private DriverRepository driverRepository;
    @Mock private DriverTruckAssignmentRepository assignmentRepository;
    @InjectMocks private UpdateDriverAvailabilityHandler handler;

    private DriverDao driver;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        driver = new DriverDao();
        UserDao user = new UserDao();
        user.setUsername("Jake");
        user.setEmail("jake@example.com");
        driver.setUser(user);
    }

    @Test
    void handle_InvalidDriverId_ShouldReturnError() {
        ApiResponse<DriverResponseDto> response = handler.Handle(null, true);
        assertFalse(response.isSuccess());
        assertEquals("Invalid driver ID", response.getMessage());
    }

    @Test
    void handle_DriverNotFound_ShouldReturnError() {
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());
        ApiResponse<DriverResponseDto> response = handler.Handle(1L, true);
        assertFalse(response.isSuccess());
        assertEquals("Driver not found", response.getMessage());
    }

    @Test
    void handle_UpdateAvailabilityToFalse_ShouldSaveDriverOnly() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        ApiResponse<DriverResponseDto> response = handler.Handle(1L, false);

        assertTrue(response.isSuccess());
        verify(driverRepository).save(driver);
        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void handle_UpdateAvailabilityToTrue_ShouldUpdateAssignments() {
        DriverTruckAssignmentDao assignment = new DriverTruckAssignmentDao();
        assignment.setAssignmentStatus(StatusEnum.ASSIGNED);

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(assignmentRepository.findAllByDriverAndAssignmentStatus(driver, StatusEnum.COMPLETED))
                .thenReturn(List.of(assignment));

        ApiResponse<DriverResponseDto> response = handler.Handle(1L, true);

        assertTrue(response.isSuccess());
        verify(driverRepository).save(driver);
        verify(assignmentRepository).save(any(DriverTruckAssignmentDao.class));
    }
}
