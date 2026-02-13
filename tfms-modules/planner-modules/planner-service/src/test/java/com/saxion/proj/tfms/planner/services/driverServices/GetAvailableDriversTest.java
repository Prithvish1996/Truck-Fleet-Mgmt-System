package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.*;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAvailableDriversTest {

    @Mock private DriverRepository driverRepository;
    @InjectMocks private GetAvailableDriversHandler handler;

    private DriverDao driver;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        driver = new DriverDao();
        UserDao user = new UserDao();
        user.setUsername("Jane");
        user.setEmail("jane@example.com");
        driver.setUser(user);
        driver.setIsAvailable(true);
    }

    @Test
    void handle_NoDrivers_ShouldReturnEmptyList() {
        when(driverRepository.findByIsAvailableTrue()).thenReturn(List.of());
        ApiResponse<List<DriverResponseDto>> response = handler.Handle();
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void handle_DriversFound_ShouldReturnMappedDtos() {
        LocationDao location = new LocationDao();
        location.setCity("Amsterdam");
        driver.setLocation(location);

        when(driverRepository.findByIsAvailableTrue()).thenReturn(List.of(driver));

        ApiResponse<List<DriverResponseDto>> response = handler.Handle();

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        DriverResponseDto dto = response.getData().get(0);
        assertEquals("Jane", dto.getUserName());
        assertEquals("Amsterdam", dto.getCity());
    }
}
