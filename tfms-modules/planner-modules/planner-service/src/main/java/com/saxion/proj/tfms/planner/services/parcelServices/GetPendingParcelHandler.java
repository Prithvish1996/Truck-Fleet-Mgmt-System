package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.WareHouseDao;
import com.saxion.proj.tfms.planner.abstractions.parcelServices.IGetPendingParcel;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import com.saxion.proj.tfms.planner.repository.WarehouseRepository;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Qualifier("getPendingParcelHandler")
@Transactional
public class GetPendingParcelHandler implements IGetPendingParcel {

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ParcelMapperHandler parcelMapper;

    /**
     * Returns a grouped list of pending parcels scheduled for the next day,
     * grouped by both warehouse name.
     */
    @Override
    public ApiResponse<Map<String, List<ParcelResponseDto>>> Handle() {

        // Fetch all warehouses first (to ensure valid references)
        Map<Long, String> warehouseLookup = warehouseRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        WareHouseDao::getId,
                        warehouse -> warehouse.getName() != null ? warehouse.getName() : "Unnamed Warehouse"
                ));

        if (warehouseLookup.isEmpty()) {
            return ApiResponse.error("No warehouses found in the system");
        }

        // Fetch all parcels with status 'pending'
        List<ParcelDao> pendingParcels = parcelRepository.findPendingWithRelations();

        if (pendingParcels.isEmpty()) {
            return ApiResponse.error("No pending parcels matched with any warehouse");
        }

        // Group by warehouse ID and Name (e.g., "1 - Central Warehouse")
        Map<String, List<ParcelResponseDto>> groupedByWarehouse = pendingParcels.stream()
                .filter(parcel -> parcel.getWarehouse() != null &&
                        warehouseLookup.containsKey(parcel.getWarehouse().getId()))
                .collect(Collectors.groupingBy(
                        parcel -> {
                            Long id = parcel.getWarehouse().getId();
                            String name = warehouseLookup.get(id);
                            return String.format("%d - %s", id, name);
                        },
                        Collectors.mapping(parcelMapper::toDto, Collectors.toList())
                ));

        if (groupedByWarehouse.isEmpty()) {
            return ApiResponse.error("No pending parcels matched with any warehouse");
        }

        return ApiResponse.success(groupedByWarehouse);
    }
}
