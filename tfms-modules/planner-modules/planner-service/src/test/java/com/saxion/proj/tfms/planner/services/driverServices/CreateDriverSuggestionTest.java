package com.saxion.proj.tfms.planner.services.driverServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.DriverSuggestionDao;
import com.saxion.proj.tfms.planner.repository.DriverRepository;
import com.saxion.proj.tfms.planner.repository.DriverSuggestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateDriverSuggestionTest {

    private DriverRepository driverRepository;
    private DriverSuggestionRepository suggestionRepository;
    private CreateDriverSuggestionHandler handler;

    @BeforeEach
    void setUp() {
        driverRepository = mock(DriverRepository.class);
        suggestionRepository = mock(DriverSuggestionRepository.class);
        handler = new CreateDriverSuggestionHandler(driverRepository, suggestionRepository);
    }

    // -------------------------
    // 1. driverId validation
    // -------------------------

    @Test
    void handle_nullDriverId_returnsInvalidDriverId() {
        ApiResponse<String> res = handler.Handle(null, "any");
        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
        verifyNoInteractions(driverRepository, suggestionRepository);
    }

    @Test
    void handle_zeroDriverId_returnsInvalidDriverId() {
        ApiResponse<String> res = handler.Handle(0L, "any");
        assertFalse(res.isSuccess());
        assertEquals("Invalid driver ID", res.getMessage());
        verifyNoInteractions(driverRepository, suggestionRepository);
    }

    // -------------------------
    // 2. driver not found
    // -------------------------

    @Test
    void handle_driverNotFound_returnsDriverNotFound() {
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse<String> res = handler.Handle(1L, "Suggestion");

        assertFalse(res.isSuccess());
        assertEquals("Driver not found", res.getMessage());
        verify(driverRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(driverRepository);
        verifyNoInteractions(suggestionRepository);
    }

    // -------------------------
    // 3. suggestion blank or null
    // -------------------------

    @Test
    void handle_nullSuggestion_returnsCannotBeEmpty() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(new DriverDao()));

        ApiResponse<String> res = handler.Handle(1L, null);

        assertFalse(res.isSuccess());
        assertEquals("Suggestion cannot be empty", res.getMessage());
        verify(driverRepository).findById(1L);
        verifyNoInteractions(suggestionRepository);
    }

    @Test
    void handle_blankSuggestion_returnsCannotBeEmpty() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(new DriverDao()));

        ApiResponse<String> res = handler.Handle(1L, "   \t\n  ");

        assertFalse(res.isSuccess());
        assertEquals("Suggestion cannot be empty", res.getMessage());
        verify(driverRepository).findById(1L);
        verifyNoInteractions(suggestionRepository);
    }

    // -------------------------
    // 4. duplicate suggestion exists
    // -------------------------

    @Test
    void handle_duplicateSuggestion_returnsDuplicateError() {
        when(driverRepository.findById(2L)).thenReturn(Optional.of(new DriverDao()));
        // existsByDriver_IdAndSuggestion is called with trimmed suggestion
        when(suggestionRepository.existsByDriver_IdAndSuggestion(eq(2L), eq("trimmed"))).thenReturn(true);

        ApiResponse<String> res = handler.Handle(2L, " trimmed ");

        assertFalse(res.isSuccess());
        assertEquals("Duplicate suggestion", res.getMessage());

        verify(driverRepository).findById(2L);
        verify(suggestionRepository).existsByDriver_IdAndSuggestion(2L, "trimmed");
        verifyNoMoreInteractions(suggestionRepository);
    }

    // -------------------------
    // 5. success path
    // -------------------------

    @Test
    void handle_validSuggestion_savesAndReturnsSuccess() {
        DriverDao driver = new DriverDao();
        when(driverRepository.findById(3L)).thenReturn(Optional.of(driver));
        when(suggestionRepository.existsByDriver_IdAndSuggestion(eq(3L), eq("good idea"))).thenReturn(false);

        ArgumentCaptor<DriverSuggestionDao> captor = ArgumentCaptor.forClass(DriverSuggestionDao.class);
        when(suggestionRepository.save(captor.capture())).thenAnswer(invocation -> {
            DriverSuggestionDao saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        ApiResponse<String> res = handler.Handle(3L, "good idea");

        assertTrue(res.isSuccess());
        assertEquals("Operation successful", res.getMessage());

        // verify repository calls
        verify(driverRepository).findById(3L);
        verify(suggestionRepository).existsByDriver_IdAndSuggestion(3L, "good idea");
        verify(suggestionRepository).save(any(DriverSuggestionDao.class));

        DriverSuggestionDao savedEntity = captor.getValue();
        assertNotNull(savedEntity);
        assertEquals("good idea", savedEntity.getSuggestion());
        assertEquals(driver, savedEntity.getDriver());
    }
}
