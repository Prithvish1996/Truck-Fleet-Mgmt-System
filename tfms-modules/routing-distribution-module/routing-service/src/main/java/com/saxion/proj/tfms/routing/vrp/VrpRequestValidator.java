package com.saxion.proj.tfms.routing.vrp;

import com.saxion.proj.tfms.routing.dto.ParcelInfo;
import com.saxion.proj.tfms.routing.dto.TruckInfo;
import com.saxion.proj.tfms.routing.dto.VrpRequestDto;
import org.springframework.stereotype.Component;

/**
 * Validates VRP requests according to business rules and constraints.
 * Single Responsibility Principle: Handles only validation logic
 */
@Component
public class VrpRequestValidator {

    public void validate(VrpRequestDto request) {
        validateNotNull(request);
        validateDepot(request);
        validateTrucks(request);
        validateParcels(request);
        validateCapacity(request);
        validateCoordinates(request);
        System.out.println("Request validation passed");
    }

    private void validateNotNull(VrpRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("VRP request cannot be null");
        }
    }

    private void validateDepot(VrpRequestDto request) {
        if (request.getDepot() == null) {
            throw new IllegalArgumentException("Depot information is required");
        }
    }

    private void validateTrucks(VrpRequestDto request) {
        if (request.getTrucks() == null || request.getTrucks().isEmpty()) {
            throw new IllegalArgumentException("At least one truck is required");
        }
    }

    private void validateParcels(VrpRequestDto request) {
        if (request.getParcels() == null) {
            throw new IllegalArgumentException("Parcels list cannot be null");
        }
    }

    private void validateCapacity(VrpRequestDto request) {
        double totalTruckCapacity = request.getTrucks().stream()
                .mapToDouble(TruckInfo::getVolume)
                .sum();

        for (ParcelInfo parcel : request.getParcels()) {
            if (parcel.getVolume() > totalTruckCapacity) {
                throw new IllegalArgumentException(
                    String.format("Parcel %s (%.2f cubic units) exceeds total capacity of all trucks (%.2f cubic units). " +
                                "Either split the parcel or add more trucks.",
                                parcel.getParcelId(), parcel.getVolume(), totalTruckCapacity)
                );
            }
        }
    }

    private void validateCoordinates(VrpRequestDto request) {
        validateLatitude(request.getDepot().getLatitude(), "depot");
        validateLongitude(request.getDepot().getLongitude(), "depot");

        for (ParcelInfo parcel : request.getParcels()) {
            validateLatitude(parcel.getWarehouseLatitude(), "warehouse for parcel " + parcel.getParcelId());
            validateLongitude(parcel.getWarehouseLongitude(), "warehouse for parcel " + parcel.getParcelId());
            validateLatitude(parcel.getDeliveryLatitude(), "delivery for parcel " + parcel.getParcelId());
            validateLongitude(parcel.getDeliveryLongitude(), "delivery for parcel " + parcel.getParcelId());
        }
    }

    private void validateLatitude(double latitude, String context) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude for " + context + ": " + latitude);
        }
    }

    private void validateLongitude(double longitude, String context) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude for " + context + ": " + longitude);
        }
    }
}
