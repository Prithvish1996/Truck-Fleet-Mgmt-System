package com.saxion.proj.tfms.routing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseRoutingResult {
    Long generatedForWarehouse;
    private List<TruckRouteInfo> truckRoutes;
}
