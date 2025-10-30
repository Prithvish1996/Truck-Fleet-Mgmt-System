package com.saxion.proj.tfms.planner.services.ScheduleServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.planner.abstractions.ScheduleService.IScheduleNextDayDelivery;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.dto.ScheduleRequestDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.services.ParcelServices.ParcelMapperHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleNextDayDeliveryHandler implements IScheduleNextDayDelivery {

    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;

    @Autowired
    public ScheduleNextDayDeliveryHandler(ParcelRepository parcelRepository,
                                          ParcelMapperHandler parcelMapper) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
    }

    @Override
    @Transactional
    public ApiResponse<List<ParcelResponseDto>> Handle(ScheduleRequestDto request) {

        // 2️Validate request input
        if (request == null || request.getParcelIds() == null || request.getParcelIds().isEmpty()) {
            return ApiResponse.error("No parcels provided for scheduling");
        }

        // 3️Compute planned delivery date (next day, skip Sunday)
        ZonedDateTime plannedDate = request.getDeliveryDate();
        if (plannedDate == null) {
            plannedDate = ZonedDateTime.now().plusDays(1);
            if (plannedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                plannedDate = plannedDate.plusDays(1);
            }
        }

        List<ParcelResponseDto> updatedParcels = new ArrayList<>();

        for (Long parcelId : request.getParcelIds()) {
            ParcelDao parcel = parcelRepository.findById(parcelId).orElse(null);
            if (parcel == null) continue;

            // Business rule i: Skip if parcel already delivered or scheduled
            if (parcel.getStatus() == StatusEnum.DELIVERED || parcel.getStatus() == StatusEnum.SCHEDULED) {
                continue;
            }

            // Business rule ii: Prevent rescheduling if planned date is already set
            if (parcel.getPlannedDeliveryDate() != null &&
                    parcel.getPlannedDeliveryDate().isEqual(plannedDate)) {
                continue;
            }

            // iii. Update parcel details
            parcel.setStatus(StatusEnum.SCHEDULED);
            parcel.setPlannedDeliveryDate(plannedDate);
            parcelRepository.save(parcel);

            // iv. Build response DTO
            ParcelResponseDto dto = new ParcelResponseDto();
            dto = parcelMapper.toDto(parcel);
            updatedParcels.add(dto);
        }

        return ApiResponse.success(updatedParcels, "Parcels successfully scheduled for next-day delivery");
    }
}
