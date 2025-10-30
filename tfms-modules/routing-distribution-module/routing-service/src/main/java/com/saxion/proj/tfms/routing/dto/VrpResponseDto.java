package com.saxion.proj.tfms.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for VRP optimization
 * Contains optimized routes for each truck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VrpResponseDto {
    
    private int totalVehiclesUsed;
    private int totalDistance;
    private int totalTime;
    private List<TruckRoute> truckRoutes;
    //  Can be either due to capacity or lack of available trucks or distance constraints [max distance radius is 300 km in a day]
    // We don,t want to fail the whole optimization if some parcels are unservable
    // So we return them in a separate list for further handling
    private List<ParcelInfo> unservableParcels;
}
