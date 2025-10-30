package com.saxion.proj.tfms.routing.vrp;

import com.saxion.proj.tfms.routing.dto.ParcelInfo;
import com.saxion.proj.tfms.routing.dto.VrpRequestDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy for grouping parcels by warehouse for independent route optimization.
 * Single Responsibility Principle: Handles warehouse-based parcel grouping
 */
@Component
public class WarehouseGroupingStrategy {

    public static class WarehouseGroup {
        private final String warehouseId;
        private final List<ParcelInfo> parcels;
        private final double totalVolume;

        public WarehouseGroup(String warehouseId, List<ParcelInfo> parcels, double totalVolume) {
            this.warehouseId = warehouseId;
            this.parcels = parcels;
            this.totalVolume = totalVolume;
        }

        public String getWarehouseId() {
            return warehouseId;
        }

        public List<ParcelInfo> getParcels() {
            return parcels;
        }

        public double getTotalVolume() {
            return totalVolume;
        }

        public boolean hasValidVolume() {
            return totalVolume > 0;
        }
    }

    public Map<String, WarehouseGroup> groupParcelsByWarehouse(VrpRequestDto request) {
        Map<String, List<ParcelInfo>> parcelsByWarehouse = new HashMap<>();
        
        for (ParcelInfo parcel : request.getParcels()) {
            parcelsByWarehouse.computeIfAbsent(parcel.getWarehouseId(), k -> new ArrayList<>())
                    .add(parcel);
        }
        
        Map<String, WarehouseGroup> warehouseGroups = new HashMap<>();
        for (Map.Entry<String, List<ParcelInfo>> entry : parcelsByWarehouse.entrySet()) {
            String warehouseId = entry.getKey();
            List<ParcelInfo> parcels = entry.getValue();
            double totalVolume = parcels.stream()
                    .mapToDouble(ParcelInfo::getVolume)
                    .sum();
            
            warehouseGroups.put(warehouseId, new WarehouseGroup(warehouseId, parcels, totalVolume));
        }
        
        return warehouseGroups;
    }

    public VrpRequestDto createWarehouseRequest(VrpRequestDto originalRequest, WarehouseGroup warehouseGroup) {
        VrpRequestDto warehouseRequest = new VrpRequestDto();
        warehouseRequest.setDepot(originalRequest.getDepot());
        warehouseRequest.setParcels(warehouseGroup.getParcels());
        warehouseRequest.setMetric(originalRequest.getMetric());
        return warehouseRequest;
    }
}
