package com.saxion.proj.tfms.planner.services.ScheduleServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.dto.ScheduleRequestDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.services.ParcelServices.ParcelMapperHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleNextDayDeliveryTests {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapperHandler parcelMapper;

    @InjectMocks
    private ScheduleNextDayDeliveryHandler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --- MC/DC: Null Request ---
    @Test
    void handle_shouldReturnErrorWhenRequestIsNull() {
        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(null);
        assertFalse(response.isSuccess());
        assertEquals("No parcels provided for scheduling", response.getMessage());
    }

    // --- MC/DC: Empty parcel list ---
    @Test
    void handle_shouldReturnErrorWhenParcelIdsEmpty() {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        dto.setParcelIds(Collections.emptyList());
        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(dto);
        assertFalse(response.isSuccess());
    }

    // --- MC/DC: Planned date null and not Sunday ---
    @Test
    void handle_shouldUseNextDayWhenDeliveryDateNullAndNotSunday() {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        dto.setParcelIds(List.of(1L));
        dto.setDeliveryDate(null);

        ParcelDao parcel = new ParcelDao();
        parcel.setId(1L);
        parcel.setStatus(StatusEnum.PENDING);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any())).thenReturn(parcel);
        when(parcelMapper.toDto(parcel)).thenReturn(new ParcelResponseDto());

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(dto);

        assertTrue(response.isSuccess());
        verify(parcelRepository, times(1)).save(parcel);
        assertEquals(StatusEnum.SCHEDULED, parcel.getStatus());
        assertNotNull(parcel.getPlannedDeliveryDate());
    }

    // --- MC/DC: Planned date null but Sunday skip ---
    @Test
    void handle_shouldSkipSundayWhenNextDayIsSunday() {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        dto.setParcelIds(List.of(1L));
        dto.setDeliveryDate(null);

        ParcelDao parcel = new ParcelDao();
        parcel.setId(1L);
        parcel.setStatus(StatusEnum.PENDING);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any())).thenReturn(parcel);
        when(parcelMapper.toDto(parcel)).thenReturn(new ParcelResponseDto());

        // Mock the current date to Saturday
        ZonedDateTime saturday = ZonedDateTime.now().with(DayOfWeek.SATURDAY);
        try (MockedStatic<ZonedDateTime> mock = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mock.when(ZonedDateTime::now).thenReturn(saturday);
            handler.Handle(dto);
        }

        verify(parcelRepository, times(1)).save(any());
    }

    // --- MC/DC: Parcel not found ---
    @Test
    void handle_shouldSkipWhenParcelNotFound() {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        dto.setParcelIds(List.of(99L));

        when(parcelRepository.findById(99L)).thenReturn(Optional.empty());

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(dto);
        assertTrue(response.getData().isEmpty());
    }

    // --- MC/DC: Parcel already delivered ---
    @Test
    void handle_shouldSkipWhenParcelDelivered() {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        dto.setParcelIds(List.of(2L));

        ParcelDao deliveredParcel = new ParcelDao();
        deliveredParcel.setId(2L);
        deliveredParcel.setStatus(StatusEnum.DELIVERED);

        when(parcelRepository.findById(2L)).thenReturn(Optional.of(deliveredParcel));

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(dto);
        assertTrue(response.getData().isEmpty());
        verify(parcelRepository, never()).save(any());
    }

    // --- MC/DC: Parcel already scheduled with same date ---
    @Test
    void handle_shouldSkipWhenAlreadyScheduledSameDate() {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        dto.setParcelIds(List.of(3L));
        dto.setDeliveryDate(ZonedDateTime.now().plusDays(1));

        ParcelDao parcel = new ParcelDao();
        parcel.setId(3L);
        parcel.setStatus(StatusEnum.PENDING);
        parcel.setPlannedDeliveryDate(dto.getDeliveryDate());

        when(parcelRepository.findById(3L)).thenReturn(Optional.of(parcel));

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(dto);
        assertTrue(response.getData().isEmpty());
        verify(parcelRepository, never()).save(any());
    }

    // --- MC/DC: Valid parcel scheduling ---
    @Test
    void handle_shouldScheduleValidParcel() {
        ScheduleRequestDto dto = new ScheduleRequestDto();
        dto.setParcelIds(List.of(5L));
        dto.setDeliveryDate(ZonedDateTime.now().plusDays(2));

        ParcelDao parcel = new ParcelDao();
        parcel.setId(5L);
        parcel.setStatus(StatusEnum.PENDING);

        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(parcel)).thenReturn(parcel);
        when(parcelMapper.toDto(parcel)).thenReturn(new ParcelResponseDto());

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(dto);

        assertTrue(response.isSuccess());
        assertEquals(StatusEnum.SCHEDULED, parcel.getStatus());
        verify(parcelRepository).save(parcel);
    }
}
