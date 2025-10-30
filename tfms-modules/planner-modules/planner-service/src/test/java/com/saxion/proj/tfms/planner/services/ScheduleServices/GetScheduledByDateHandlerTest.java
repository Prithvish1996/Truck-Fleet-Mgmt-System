package com.saxion.proj.tfms.planner.services.ScheduleServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.utility.Helper;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.services.ParcelServices.ParcelMapperHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetScheduledByDateHandlerTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private Helper helper;

    @Mock
    private ParcelMapperHandler parcelMapper;

    @InjectMocks
    private GetScheduledByDateHandler handler;

    private ParcelDao parcel1;
    private ParcelDao parcel2;
    private ParcelResponseDto dto1;
    private ParcelResponseDto dto2;
    private ZonedDateTime date;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample data setup
        date = ZonedDateTime.of(LocalDateTime.of(2025, 10, 29, 10, 0), ZoneId.systemDefault());
        parcel1 = new ParcelDao();
        parcel1.setId(1L);
        parcel1.setName("Parcel A");
        parcel1.setStatus(StatusEnum.SCHEDULED);

        parcel2 = new ParcelDao();
        parcel2.setId(2L);
        parcel2.setName("Parcel B");
        parcel2.setStatus(StatusEnum.SCHEDULED);

        dto1 = new ParcelResponseDto();
        dto1.setName("Parcel A");

        dto2 = new ParcelResponseDto();
        dto2.setName("Parcel B");
    }

    // Test 1: plannedDate is null — helper should compute next valid date
    @Test
    void handle_PlannedDateNull_ShouldUseComputedDate() {
        ZonedDateTime computedDate = date.plusDays(1);
        when(helper.ComputePlannedDate(null)).thenReturn(computedDate);
        when(parcelRepository.findAllByStatusAndPlannedDeliveryDate(StatusEnum.SCHEDULED, computedDate.toLocalDate()))
                .thenReturn(List.of(parcel1));
        when(parcelMapper.toDto(parcel1)).thenReturn(dto1);

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(null);

        verify(helper, times(1)).ComputePlannedDate(null);
        verify(parcelRepository, times(1))
                .findAllByStatusAndPlannedDeliveryDate(StatusEnum.SCHEDULED, computedDate.toLocalDate());
        verify(parcelMapper, times(1)).toDto(parcel1);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("Parcel A", response.getData().get(0).getName());
    }

    // Test 2: plannedDate provided — use as is
    @Test
    void handle_PlannedDateProvided_ShouldUseGivenDate() {
        when(helper.ComputePlannedDate(date)).thenReturn(date);
        when(parcelRepository.findAllByStatusAndPlannedDeliveryDate(StatusEnum.SCHEDULED, date.toLocalDate()))
                .thenReturn(List.of(parcel1, parcel2));
        when(parcelMapper.toDto(parcel1)).thenReturn(dto1);
        when(parcelMapper.toDto(parcel2)).thenReturn(dto2);

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(date);

        verify(helper, times(1)).ComputePlannedDate(date);
        verify(parcelRepository, times(1))
                .findAllByStatusAndPlannedDeliveryDate(StatusEnum.SCHEDULED, date.toLocalDate());
        verify(parcelMapper, times(2)).toDto(any());

        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
    }

    // Test 3: Repository returns empty list
    @Test
    void handle_NoScheduledParcels_ShouldReturnEmptyList() {
        when(helper.ComputePlannedDate(date)).thenReturn(date);
        when(parcelRepository.findAllByStatusAndPlannedDeliveryDate(StatusEnum.SCHEDULED, date.toLocalDate()))
                .thenReturn(Collections.emptyList());

        ApiResponse<List<ParcelResponseDto>> response = handler.Handle(date);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().size());
        verify(parcelMapper, never()).toDto(any());
    }
}
