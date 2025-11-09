package com.saxion.proj.tfms.planner.services.parcelServices;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.ParcelDao;
import com.saxion.proj.tfms.planner.abstractions.parcelServices.IGetAllParcels;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import com.saxion.proj.tfms.planner.repository.ParcelRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Qualifier("getAllParcelHandler")
public class GetAllParcelHandler implements IGetAllParcels {

    private final ParcelRepository parcelRepository;
    private final ParcelMapperHandler parcelMapper;

    public GetAllParcelHandler(ParcelRepository parcelRepository, ParcelMapperHandler parcelMapper) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
    }

    /**
     * Retrieves paginated list of parcels for a specific warehouse.
     * Optionally filters parcels by name, city, recipient name, or recipient phone.
     *
     * @param warehouseId Required warehouse ID.
     * @param searchText Optional search text for parcel name, city, recipient name, or phone.
     * @param page Page number (0-based).
     * @param size Number of items per page.
     */
    @Override
    public ApiResponse<Map<String, Object>> Handle(Long warehouseId, String searchText, int page, int size) {

        // Validate warehouse ID
        if (warehouseId == null || warehouseId <= 0) {
            return ApiResponse.error("Invalid warehouse ID");
        }

        if (page < 0 || size <= 0) {
            return ApiResponse.error("Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(page, size);

        // Step 1: get page of parcel IDs
        List<Long> ids = parcelRepository.findParcelIds(pageable);
        if (ids.isEmpty()) {
            Map<String, Object> emptyResponse = new HashMap<>();
            emptyResponse.put("currentPage", page);
            emptyResponse.put("pageSize", size);
            emptyResponse.put("totalItems", 0);
            emptyResponse.put("totalPages", 0);
            emptyResponse.put("data", Collections.emptyList());
            return ApiResponse.success(emptyResponse);
        }

        // Step 2: fetch parcels with eager relationships
        List<ParcelDao> parcels = parcelRepository.findAllWithLocations(ids);

        // Step 3: filtering
        String filterText = (searchText != null) ? searchText.toLowerCase() : "";
        List<ParcelResponseDto> filteredDtos = parcels.stream()
                .filter(parcel -> parcel.getWarehouse() != null && warehouseId.equals(parcel.getWarehouse().getId()))
                .filter(parcel -> {
                    if (filterText.isEmpty()) return true;
                    return (parcel.getName() != null && parcel.getName().toLowerCase().startsWith(filterText))
                            || (parcel.getStatus() != null && parcel.getStatus().name().toLowerCase().startsWith(filterText))
                            || (parcel.getRecipientName() != null && parcel.getRecipientName().toLowerCase().startsWith(filterText))
                            || (parcel.getRecipientPhone() != null && parcel.getRecipientPhone().toLowerCase().startsWith(filterText));
                })
                .map(parcelMapper::toDto)
                .collect(Collectors.toList());

        // Step 4: get total count for pagination metadata
        long totalCount = parcelRepository.count();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // Step 5: build response map
        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalItems", totalCount);
        response.put("totalPages", totalPages);
        response.put("data", filteredDtos);

        return ApiResponse.success(response);
    }

}
