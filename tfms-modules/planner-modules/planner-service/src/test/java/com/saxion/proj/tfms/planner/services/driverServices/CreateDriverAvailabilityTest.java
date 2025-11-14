package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverAvailabilityDao;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.planner.repository.DriverAvailabilityRepository;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateDriverAvailabilityTest {

    private DriverRepository driverRepository;
    private DriverAvailabilityRepository availabilityRepository;
    private CreateDriverAvailabilityHandler handler;

    @BeforeEach
    void setup() {
        driverRepository = mock(DriverRepository.class);
        availabilityRepository = mock(DriverAvailabilityRepository.class);
        handler = new CreateDriverAvailabilityHandler(driverRepository, availabilityRepository);
    }

    // ---------------------------------------------------------
    // 1. DRIVER ID VALIDATION (MC/DC)
    // ---------------------------------------------------------

    @Test
    void handleInvalidDriverId_null() {
        ApiResponse<String> res = handler.Handle(null, List.of(ZonedDateTime.now().plusDays(1)));
        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
    }

    @Test
    void handle_InvalidDriverId_zero() {
        ApiResponse<String> res = handler.Handle(0L, List.of(ZonedDateTime.now().plusDays(1)));
        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
    }

    @Test
    void handle_InvalidDriverId_negative() {
        ApiResponse<String> res = handler.Handle(-1L, List.of(ZonedDateTime.now().plusDays(1)));
        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
    }

    // ---------------------------------------------------------
    // 2. DRIVER NOT FOUND
    // ---------------------------------------------------------

    @Test
    void handle_DriverNotFound() {
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<String> res = handler.Handle(1L, List.of(ZonedDateTime.now().plusDays(1)));

        assertFalse(res.isSuccess());
        assertEquals("Driver not found", res.getMessage());
    }

    // ---------------------------------------------------------
    // 3. INVALID DATE LIST
    // ---------------------------------------------------------

    @Test
    void handle_atesNull() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(new DriverDao()));

        ApiResponse<String> res = handler.Handle(1L, null);

        assertFalse(res.isSuccess());
        assertEquals("Availability date list cannot be empty", res.getMessage());
    }

    @Test
    void testDatesEmpty() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(new DriverDao()));

        ApiResponse<String> res = handler.Handle(1L, Collections.emptyList());

        assertFalse(res.isSuccess());
        assertEquals("Availability date list cannot be empty", res.getMessage());
    }

    // ---------------------------------------------------------
    // 4. PAST DATE VALIDATION
    // ---------------------------------------------------------

    @Test
    void handle_PastDateRejected() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(new DriverDao()));

        ZonedDateTime past = ZonedDateTime.now().minusDays(1);

        ApiResponse<String> res = handler.Handle(1L, List.of(past));

        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("Date must not be in the past"));
    }

    // ---------------------------------------------------------
    // 5. SUCCESSFUL CASE
    // ---------------------------------------------------------

    @Test
    void handle_Success() {
        DriverDao driver = new DriverDao();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        ZonedDateTime future = ZonedDateTime.now().plusDays(1);

        ApiResponse<String> res = handler.Handle(1L, List.of(future));

        assertTrue(res.isSuccess());
        assertEquals("Operation successful", res.getMessage());

        ArgumentCaptor<List<DriverAvailabilityDao>> captor = ArgumentCaptor.forClass(List.class);
        verify(availabilityRepository, times(1)).saveAll(captor.capture());

        List<DriverAvailabilityDao> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals(future, saved.get(0).getAvailableAt());
        assertEquals(driver, saved.get(0).getDriver());
        assertEquals(StatusEnum.AVAILABLE, saved.get(0).getStatus());
    }

    // ---------------------------------------------------------
    // 6. DUPLICATE DATES REMOVED
    // ---------------------------------------------------------

    @Test
    void handle_DuplicateDatesDeduplicated() {
        DriverDao driver = new DriverDao();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        ZonedDateTime d = ZonedDateTime.now().plusDays(2);

        ApiResponse<String> res = handler.Handle(1L, List.of(d, d, d));

        assertTrue(res.isSuccess());
        assertEquals("Operation successful", res.getMessage());

        ArgumentCaptor<List<DriverAvailabilityDao>> captor = ArgumentCaptor.forClass(List.class);
        verify(availabilityRepository).saveAll(captor.capture());

        assertEquals(1, captor.getValue().size());
    }
}
