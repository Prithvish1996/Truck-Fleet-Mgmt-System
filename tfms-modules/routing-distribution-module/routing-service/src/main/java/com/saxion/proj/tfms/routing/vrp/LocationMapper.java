package com.saxion.proj.tfms.routing.vrp;

import com.saxion.proj.tfms.routing.dto.ParcelInfo;
import com.saxion.proj.tfms.routing.dto.VrpRequestDto;
import com.saxion.proj.tfms.routing.service.DistanceMatrixService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps parcels and depot to location indices for OR-Tools optimization.
 * Single Responsibility Principle: Handles only location mapping and indexing
 */
@Component
public class LocationMapper {

    public static class LocationMapping {
        private final List<DistanceMatrixService.Location> locations;
        private final Map<String, Integer> locationIndices;
        private final int depotIndex;

        public LocationMapping() {
            this.locations = new ArrayList<>();
            this.locationIndices = new HashMap<>();
            this.depotIndex = 0;
        }

        public List<DistanceMatrixService.Location> getLocations() {
            return locations;
        }

        public Map<String, Integer> getLocationIndices() {
            return locationIndices;
        }

        public int getDepotIndex() {
            return depotIndex;
        }

        void addLocation(String key, DistanceMatrixService.Location location, int index) {
            locations.add(location);
            locationIndices.put(key, index);
        }
    }

    public LocationMapping buildLocationMapping(VrpRequestDto request) {
        LocationMapping mapping = new LocationMapping();

        DistanceMatrixService.Location depot = new DistanceMatrixService.Location(
                request.getDepot().getLatitude(),
                request.getDepot().getLongitude()
        );
        mapping.addLocation("depot", depot, 0);

        int currentIndex = 1;

        for (ParcelInfo parcel : request.getParcels()) {
            String pickupKey = "pickup_" + parcel.getParcelId();
            if (!mapping.locationIndices.containsKey(pickupKey)) {
                DistanceMatrixService.Location pickupLocation = new DistanceMatrixService.Location(
                        parcel.getWarehouseLatitude(),
                        parcel.getWarehouseLongitude()
                );
                mapping.addLocation(pickupKey, pickupLocation, currentIndex++);
            }

            String deliveryKey = "delivery_" + parcel.getParcelId();
            if (!mapping.locationIndices.containsKey(deliveryKey)) {
                DistanceMatrixService.Location deliveryLocation = new DistanceMatrixService.Location(
                        parcel.getDeliveryLatitude(),
                        parcel.getDeliveryLongitude()
                );
                mapping.addLocation(deliveryKey, deliveryLocation, currentIndex++);
            }
        }

        return mapping;
    }

    public String getLocationKey(int nodeIndex, LocationMapping mapping) {
        for (Map.Entry<String, Integer> entry : mapping.locationIndices.entrySet()) {
            if (entry.getValue() == nodeIndex) {
                return entry.getKey();
            }
        }
        return null;
    }
}
