package com.saxion.proj.tfms.commons.utility.depotwarehouse.service;


import com.saxion.proj.tfms.commons.utility.depotwarehouse.helper.GraphHopperUtil;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.TruckWarehouseAssignment;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.Depot;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.Truck;
import com.saxion.proj.tfms.commons.utility.depotwarehouse.model.Warehouse;

import java.util.ArrayList;
import java.util.List;

public class AssignmentService {

    private final GraphHopperUtil routingUtil;
    private static final double DIST_THRESHOLD_KM = 10.0;
    private static final double TIME_THRESHOLD_MIN = 30.0;

    public AssignmentService(GraphHopperUtil routingUtil) {
        this.routingUtil = routingUtil;
    }

    /** Assign trucks from depots to serviceable warehouses */
    public List<TruckWarehouseAssignment> assignTrucks(List<Depot> depots, List<Warehouse> warehouses) {
        List<TruckWarehouseAssignment> truckWarehouseAssignments = new ArrayList<>();

        for (Depot depot : depots) {
            for (Warehouse wh : warehouses) {
                double distance = routingUtil.computeDistance(depot.lat, depot.lon, wh.lat, wh.lon);
                double time = routingUtil.computeTime(depot.lat, depot.lon, wh.lat, wh.lon);

                if (distance <= DIST_THRESHOLD_KM && time <= TIME_THRESHOLD_MIN) {
                    Truck truck = getAvailableTruck(depot);
                    if (truck != null) {
                        truck.assigned = true;
                        truck.currentLocation = wh;
                        truckWarehouseAssignments.add(new TruckWarehouseAssignment(depot, wh, truck, distance, time));
                        System.out.println("Assigned: " + truck.id + " | Route: " + depot.name + " -> " + wh.name);
                    }
                }
            }
        }
        return truckWarehouseAssignments;
    }

    /** Get first unassigned truck from depot */
    private Truck getAvailableTruck(Depot depot) {
        for (Truck truck : depot.trucks) {
            if (!truck.assigned) return truck;
        }
        return null;
    }
}
