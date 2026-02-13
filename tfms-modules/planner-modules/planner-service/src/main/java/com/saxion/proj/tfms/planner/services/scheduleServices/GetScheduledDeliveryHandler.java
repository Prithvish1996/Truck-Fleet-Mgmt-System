package com.saxion.proj.tfms.planner.services.scheduleServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.commons.utility.PlannerHelper;
import com.saxion.proj.tfms.planner.abstractions.scheduleService.IGetScheduledDeliveries;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.services.parcelServices.ParcelMapperHandler;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Qualifier("getScheduledDeliveryHandler")
@Transactional
public class GetScheduledDeliveryHandler implements IGetScheduledDeliveries {

    private final ParcelRepository parcelRepository;
    private final PlannerHelper helper;
    private final ParcelMapperHandler parcelMapper;

    public GetScheduledDeliveryHandler(ParcelRepository parcelRepository,
                                       PlannerHelper helper,
                                       ParcelMapperHandler parcelMapper) {
        this.parcelRepository = parcelRepository;
        this.helper = helper;
        this.parcelMapper = parcelMapper;
    }

    // This handle return the list of scheduled parcels by date and the list is paginated.
    @Override
    public ApiResponse<Map<String, Object>> Handle(ZonedDateTime plannedDate, int page, int size) {

        if (page < 0 || size <= 0) {
            return ApiResponse.error("Invalid pagination parameters");
        }

        // Step 1 Compute effective planned delivery date (skip Sunday)
        ZonedDateTime effectiveDate = helper.ComputePlannedDate(plannedDate);
        ZonedDateTime startOfDay = effectiveDate.toLocalDate().atStartOfDay(effectiveDate.getZone());
        ZonedDateTime endOfDay = startOfDay.plusDays(1);

        List<ParcelDao> parcels = parcelRepository.findAllByStatusAndPlannedDeliveryDate(
                StatusEnum.SCHEDULED,
                startOfDay,
                endOfDay
        );

        List<ParcelResponseDto> scheduleParcels = parcels.stream()
                .filter(parcel -> parcel.getWarehouse() != null)
                .map(parcelMapper::toDto)
                .collect(Collectors.toList());

        // Step 2: get total count for pagination metadata
        long totalCount = scheduleParcels.size();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // Step 3 & 4: Calculate start and end index for current page
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, scheduleParcels.size());

        List<ParcelResponseDto> pagedParcels = new ArrayList<>();
        if (fromIndex < scheduleParcels.size()) {
            pagedParcels = scheduleParcels.subList(fromIndex, toIndex);
        }

        // Step 5: Build response map
        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalItems", totalCount);
        response.put("totalPages", totalPages);
        response.put("data", pagedParcels);

        return ApiResponse.success(response);
    }
}