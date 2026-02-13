package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.LocationDao;
import com.saxion.proj.tfms.commons.model.UserDao;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetDriverHandlerTest {

    private DriverRepository driverRepository;
    private GetDriverHandler handler;

    @BeforeEach
    void setUp() {
        driverRepository = mock(DriverRepository.class);
        handler = new GetDriverHandler(driverRepository);
    }

    // -------------------------
    // 1. driverId validation
    // -------------------------
    @Test
    void handle_nullDriverId_returnsInvalidDriverId() {
        ApiResponse<?> res = handler.Handle(null);

        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
        verifyNoInteractions(driverRepository);
    }

    @Test
    void handle_zeroDriverId_returnsInvalidDriverId() {
        ApiResponse<?> res = handler.Handle(0L);

        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
        verifyNoInteractions(driverRepository);
    }

    @Test
    void handle_negativeDriverId_returnsInvalidDriverId() {
        ApiResponse<?> res = handler.Handle(-5L);

        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
        verifyNoInteractions(driverRepository);
    }

    // -------------------------
    // 2. driver not found
    // -------------------------
    @Test
    void handle_driverNotFound_returnsDriverNotFound() {
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<?> res = handler.Handle(1L);

        assertFalse(res.isSuccess());
        assertEquals("Driver not found", res.getMessage());
        verify(driverRepository).findById(1L);
    }

    // -------------------------
    // 3. driver exists → success
    // -------------------------
    @Test
    void handle_driverFound_returnsDriverResponseDto() {
        UserDao user = new UserDao();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");

        LocationDao location = new LocationDao();
        location.setCity("Amsterdam");
        location.setAddress("Main Street 1");

        DriverDao driver = new DriverDao();
        driver.setId(10L);
        driver.setUser(user);
        driver.setLocation(location);
        driver.setIsAvailable(true);

        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));

        ApiResponse<DriverResponseDto> res = handler.Handle(10L);

        assertTrue(res.isSuccess());
        assertNotNull(res.getData());

        var dto = res.getData();
        assertEquals(10L, dto.getId());
        assertEquals("johndoe", dto.getUserName());
        assertEquals("john@example.com", dto.getEmail());
        assertTrue(dto.getIsAvailable());
        assertEquals("Amsterdam", dto.getCity());
        assertEquals("Main Street 1", dto.getAddress());

        verify(driverRepository).findById(10L);
    }
}
