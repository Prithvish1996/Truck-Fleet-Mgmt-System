package com.saxion.proj.tfms.planner.services.ScheduleServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.utility.Helper;
import com.saxion.proj.tfms.planner.abstractions.ScheduleService.IGetScheduledByDate;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.services.ParcelServices.ParcelMapperHandler;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetScheduledByDateHandler implements IGetScheduledByDate {

    private final ParcelRepository parcelRepository;
    private final Helper helper;
    private final ParcelMapperHandler parcelMapper;

    public GetScheduledByDateHandler(ParcelRepository parcelRepository,
                                          Helper helper,
                                          ParcelMapperHandler parcelMapper) {
        this.parcelRepository = parcelRepository;
        this.helper = helper;
        this.parcelMapper = parcelMapper;
    }

    // This handle return the list of scheduled parcels by date. The list is not paginated.
    @Override
    public ApiResponse<List<ParcelResponseDto>> Handle(ZonedDateTime plannedDate) {

        // Compute effective planned delivery date (skip Sunday)
        ZonedDateTime effectiveDate = helper.ComputePlannedDate(plannedDate);

        // Fetch paginated results directly from DB
        List<ParcelDao> scheduleParcels = parcelRepository.findAllByStatusAndPlannedDeliveryDate(
                StatusEnum.SCHEDULED,
                effectiveDate.toLocalDate()
        );

        // Map each ParcelDao to ParcelResponseDto
        List<ParcelResponseDto> parcelDtos = scheduleParcels.stream()
                .map(parcelMapper::toDto)
                .collect(Collectors.toList());

        // Return success response
        return ApiResponse.success(parcelDtos, "Scheduled parcels retrieved successfully");
    }


}