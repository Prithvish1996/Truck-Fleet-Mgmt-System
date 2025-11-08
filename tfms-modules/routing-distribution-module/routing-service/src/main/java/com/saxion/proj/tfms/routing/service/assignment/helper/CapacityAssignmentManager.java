package com.saxion.proj.tfms.routing.service.assignment.helper;

import com.saxion.proj.tfms.routing.model.Parcel;
import com.saxion.proj.tfms.routing.model.TruckInfo;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.service.TruckAssingmentAlgoService;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CapacityAssignmentManager {

    private static final Logger log = LoggerFactory.getLogger(CapacityAssignmentManager.class);

    private final IGetAllTrucksAvailable getAllTrucksAvailable;
    private final TruckAssingmentAlgoService truckAssingmentAlgoService;
    private final IMarkTruckUnavailable markTruckUnavailable;

    @Autowired
    public CapacityAssignmentManager(
            @Qualifier("GetAllTrucksAvailableCheckForIsAvailable") IGetAllTrucksAvailable getAllTrucksAvailable,
            @Qualifier("TruckAssignmentServiceByBestFitAlgorithm") TruckAssingmentAlgoService truckAssingmentAlgoService,
            @Qualifier("MarkTruckUnavailableUsingKeys") IMarkTruckUnavailable markTruckUnavailable) {
        this.getAllTrucksAvailable = getAllTrucksAvailable;
        this.truckAssingmentAlgoService = truckAssingmentAlgoService;
        this.markTruckUnavailable = markTruckUnavailable;
    }


    @Transactional
    public Map<Long, AssignmentResponse> manageAssignmentPerWarehouse(VRPRequest vrpRequest) {
        List<Parcel> allParcels = vrpRequest.getParcels();
        Map<Long, List<Parcel>> parcelsByWarehouse = batchParcelsByWarehouse(allParcels);
        Map<Long, AssignmentResponse> warehouseAssignments = new HashMap<>();
        Set<String> usedTruckNames = new HashSet<>();

        for (Map.Entry<Long, List<Parcel>> entry : parcelsByWarehouse.entrySet()) {
            Long warehouseId = entry.getKey();
            List<Parcel> warehouseParcels = entry.getValue();

            if (isWarehouseParcelsEmpty(warehouseId, warehouseParcels)) {
                continue;
            }

            List<TruckInfo> availableTrucks = getFilteredAvailableTrucks(usedTruckNames);
            if (availableTrucks.isEmpty()) {
                log.warn("No available trucks remaining for warehouse {}.", warehouseId);
                continue;
            }

            AssignmentResponse response = assignTrucksToParcels(availableTrucks, warehouseParcels);
            processAssignmentResult(warehouseId, response, warehouseAssignments, usedTruckNames);
        }

        return warehouseAssignments;
    }


    private boolean isWarehouseParcelsEmpty(Long warehouseId, List<Parcel> warehouseParcels) {
        if (warehouseParcels == null || warehouseParcels.isEmpty()) {
            log.warn("Warehouse {} has no parcels to assign.", warehouseId);
            return true;
        }
        return false;
    }


    private List<TruckInfo> getFilteredAvailableTrucks(Set<String> usedTruckNames) {
        List<TruckInfo> filtered = new ArrayList<>();
        List<TruckInfo> allTrucks = getAllTrucksAvailable.getAvailableTrucks();
        for (TruckInfo t : allTrucks) {
            if (!usedTruckNames.contains(t.getTruckName())) {
                filtered.add(t);
            }
        }
        return filtered;
    }


    private AssignmentResponse assignTrucksToParcels(List<TruckInfo> trucks, List<Parcel> parcels) {
        List<Pair<String, Double>> truckPairs = truckVolumePairs(trucks);
        List<Pair<String, Double>> parcelPairs = parcelVolumePairs(parcels);
        return truckAssingmentAlgoService.assignParcelsToTrucks(truckPairs, parcelPairs);
    }


    private void processAssignmentResult(Long warehouseId,
                                         AssignmentResponse response,
                                         Map<Long, AssignmentResponse> assignments,
                                         Set<String> usedTruckNames) {
        if (!response.isSuccess()) {
            log.warn("Assignment failed for warehouse {}.", warehouseId);
            return;
        }

        assignments.put(warehouseId, response);

        for (TruckAssignment truckAssignment : response.getTruckAssignments()) {
            String truckName = truckAssignment.getTruckPlateNumber();
            if (truckName == null || truckName.trim().isEmpty()) {
                log.warn("Skipping truck with null or empty name in warehouse {}", warehouseId);
                continue;
            }
            markTruckUnavailableSafe(truckName, warehouseId, usedTruckNames);
        }
    }


    private void markTruckUnavailableSafe(String truckName, Long warehouseId, Set<String> usedTruckNames) {
        try {
            markTruckUnavailable.byTruckUniqueName(truckName);
            usedTruckNames.add(truckName);
            log.info("Truck '{}' marked unavailable after assignment to warehouse {}", truckName, warehouseId);
        } catch (Exception e) {
            log.error("Failed to mark truck '{}' unavailable: {}", truckName, e.getMessage());
        }
    }


    private Map<Long, List<Parcel>> batchParcelsByWarehouse(List<Parcel> parcels) {
        Map<Long, List<Parcel>> map = new HashMap<>();
        for (Parcel parcel : parcels) {
            Long wid = parcel.getWarehouseId();
            if (wid == null || wid == 0  || wid<0) continue;
            if (!map.containsKey(wid)) {
                map.put(wid, new ArrayList<>());
            }
            map.get(wid).add(parcel);
        }
        return map;
    }


    private List<Pair<String, Double>> parcelVolumePairs(List<Parcel> parcels) {
        List<Pair<String, Double>> pairs = new ArrayList<>();
        for (Parcel p : parcels) {
            pairs.add(Pair.of(p.getParcelName(), p.getVolume()));
        }
        return pairs;
    }


    private List<Pair<String, Double>> truckVolumePairs(List<TruckInfo> trucks) {
        List<Pair<String, Double>> pairs = new ArrayList<>();
        for (TruckInfo t : trucks) {
            pairs.add(Pair.of(t.getTruckName(), t.getVolume()));
        }
        return pairs;
    }
}
