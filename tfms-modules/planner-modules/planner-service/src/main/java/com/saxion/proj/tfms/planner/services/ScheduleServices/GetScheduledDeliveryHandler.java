package com.saxion.proj.tfms.planner.services.ScheduleServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.utility.Helper;
import com.saxion.proj.tfms.planner.abstractions.ScheduleService.IGetScheduledDeliveries;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class GetScheduledDeliveryHandler implements IGetScheduledDeliveries {

    private final ParcelRepository parcelRepository;
    private final Helper helper;

    public GetScheduledDeliveryHandler(ParcelRepository parcelRepository,
                                       Helper helper) {
        this.parcelRepository = parcelRepository;
        this.helper = helper;
    }

    // This handle return the list of scheduled parcels by date and the list is paginated.
    @Override
    public ApiResponse<Map<String, Object>> Handle(ZonedDateTime plannedDate, int page, int size) {

        if (page < 0 || size <= 0) {
            return ApiResponse.error("Invalid pagination parameters");
        }

        // Compute effective planned delivery date (skip Sunday)
        ZonedDateTime effectiveDate = helper.ComputePlannedDate(plannedDate);

        Pageable pageable = PageRequest.of(page, size);

        // Fetch paginated results directly from DB
        Page<ParcelDao> parcelPage = parcelRepository.findByStatusAndPlannedDeliveryDate(
                StatusEnum.SCHEDULED,
                effectiveDate.toLocalDate(),
                pageable
        );

        // Prepare response map
        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", parcelPage.getNumber());
        response.put("pageSize", parcelPage.getSize());
        response.put("totalItems", parcelPage.getTotalElements());
        response.put("totalPages", parcelPage.getTotalPages());
        response.put("data", parcelPage.getContent());

        return ApiResponse.success(response);
    }
}