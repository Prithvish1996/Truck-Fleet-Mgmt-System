package com.saxion.proj.tfms.planner.services.ParcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.IGetNextDayParcelSchedule;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.services.ParcelServices.ParcelMapperHandler;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GetNextDayParcelScheduleHandler implements IGetNextDayParcelSchedule {

    private final ParcelRepository parcelRepository;
    //private final WarehouseRepository warehouseRepository;
    private final ParcelMapperHandler parcelMapper;

    public GetNextDayParcelScheduleHandler(ParcelRepository parcelRepository,
                                    WarehouseRepository warehouseRepository,
                                    ParcelMapperHandler parcelMapper) {
        this.parcelRepository = parcelRepository;
        //this.warehouseRepository = warehouseRepository;
        this.parcelMapper = parcelMapper;
    }

    /**
     * Returns a grouped list of pending parcels scheduled for the next day,
     * grouped by both warehouse name.
     */
    @Override
    public ApiResponse<Map<String, List<ParcelResponseDto>>> Handle() {

        // Fetch all parcels with status 'pending'
        List<ParcelDao> pendingParcels = parcelRepository.findAll()
                .stream()
                .filter(parcel -> "pending".equalsIgnoreCase(parcel.getStatus()))
                .collect(Collectors.toList());

        if (pendingParcels.isEmpty()) {
            return ApiResponse.error("No pending parcels scheduled for next day");
        }

        // Group by warehouse ID and name (e.g. "1 - Main Warehouse")
        Map<String, List<ParcelResponseDto>> groupedByWarehouse = pendingParcels.stream()
                .filter(parcel -> parcel.getWarehouse() != null)
                .collect(Collectors.groupingBy(
                        parcel -> {
                            var warehouse = parcel.getWarehouse();
                            Long id = warehouse.getId();
                            String name = warehouse.getName();
                            return String.format("%d - %s", id, name != null ? name : "Unnamed Warehouse");
                        },
                        Collectors.mapping(parcelMapper::toDto, Collectors.toList())
                ));

        return ApiResponse.success(groupedByWarehouse);
    }
}
