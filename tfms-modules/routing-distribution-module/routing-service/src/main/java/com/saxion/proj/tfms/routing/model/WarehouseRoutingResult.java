package com.saxion.proj.tfms.routing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRoutingResult {
    Long generatedForWarehouse;
    private List<TruckRouteInfo> truckRoutes;
}
