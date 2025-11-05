package com.saxion.proj.tfms.planner.services.ScheduleServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.utility.Helper;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetScheduledDeliveryTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private Helper helper;

    @InjectMocks
    private GetScheduledDeliveryHandler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        date = ZonedDateTime.of(LocalDateTime.of(2025, 10, 29, 10, 0), ZoneId.systemDefault());
    }

    private ZonedDateTime date;

    // --- MC/DC: Invalid page or size ---
    @Test
    void handle_shouldReturnErrorWhenInvalidPagination() {
        ApiResponse<Map<String, Object>> response = handler.Handle(ZonedDateTime.now(), -1, 10);
        assertFalse(response.isSuccess());

        response = handler.Handle(ZonedDateTime.now(), 0, 0);
        assertFalse(response.isSuccess());
    }

    // --- MC/DC: Planned date null ---
    @Test
    void handle_shouldUseNextDayWhenPlannedDateNull() {

        ZonedDateTime computedDate = date.plusDays(1);
        when(helper.ComputePlannedDate(null)).thenReturn(computedDate);

        Pageable pageable = PageRequest.of(0, 5);
        Page<ParcelDao> mockPage = new PageImpl<>(List.of(new ParcelDao()), pageable, 1);
        when(parcelRepository.findByStatusAndPlannedDeliveryDate(eq(StatusEnum.SCHEDULED), any(), eq(pageable)))
                .thenReturn(mockPage);

        ApiResponse<Map<String, Object>> response = handler.Handle(null, 0, 5);

        assertTrue(response.isSuccess());
        assertEquals(1L, response.getData().get("totalItems"));
        verify(parcelRepository).findByStatusAndPlannedDeliveryDate(eq(StatusEnum.SCHEDULED), any(), eq(pageable));
    }

    // --- MC/DC: Planned date is Sunday ---
    @Test
    void handle_shouldSkipSundayWhenPlannedDateIsSunday() {
        ZonedDateTime sunday = ZonedDateTime.now().with(DayOfWeek.SUNDAY);
        when(helper.ComputePlannedDate(sunday)).thenReturn(date);

        Pageable pageable = PageRequest.of(0, 5);
        Page<ParcelDao> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(parcelRepository.findByStatusAndPlannedDeliveryDate(any(), any(), eq(pageable)))
                .thenReturn(mockPage);

        handler.Handle(sunday, 0, 5);

        verify(parcelRepository, times(1)).findByStatusAndPlannedDeliveryDate(any(), any(), any());
    }

    // --- MC/DC: Valid planned date ---
    @Test
    void handle_shouldReturnPaginatedResult() {
        Pageable pageable = PageRequest.of(1, 2);
        ParcelDao parcel = new ParcelDao();
        Page<ParcelDao> pageResult = new PageImpl<>(List.of(parcel), pageable, 5);

        when(helper.ComputePlannedDate(date)).thenReturn(date);

        when(parcelRepository.findByStatusAndPlannedDeliveryDate(eq(StatusEnum.SCHEDULED), any(), eq(pageable)))
                .thenReturn(pageResult);

        ApiResponse<Map<String, Object>> response = handler.Handle(date, 1, 2);

        assertTrue(response.isSuccess());
        assertEquals(5L, response.getData().get("totalItems"));
        assertEquals(1, response.getData().get("currentPage"));
        assertEquals(2, response.getData().get("pageSize"));
    }
}
