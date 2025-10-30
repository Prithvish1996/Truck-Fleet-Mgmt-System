package com.saxion.proj.tfms.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for VRP optimization
 * Contains all data needed to optimize truck routes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VrpRequestDto {
    private DepotInfo depot;
    private List<TruckInfo> trucks;
    private List<ParcelInfo> parcels;
    private VrpMetric metric = VrpMetric.DISTANCE;
}
