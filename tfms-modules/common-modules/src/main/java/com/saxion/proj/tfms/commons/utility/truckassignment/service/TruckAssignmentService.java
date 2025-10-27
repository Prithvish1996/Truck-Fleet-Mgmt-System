package com.saxion.proj.tfms.commons.utility.truckassignment.service;

import com.saxion.proj.tfms.commons.utility.truckassignment.response.AssignmentResponse;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public interface TruckAssignmentService {

    /**
     * Assign parcels to trucks based on their IDs and volumes.
     * Returns a response with success status and either assignment results or error details.
     *
     * @param trucks List of trucks (truckId, capacity)
     * @param parcels List of parcels (parcelId, volume)
     * @return TruckWarehouseAssignment response with success/error status
     */
    AssignmentResponse assignParcelsToTrucks(
            List<Pair<String, Double>> trucks,
            List<Pair<String, Double>> parcels
    );
}
