package com.saxion.proj.tfms.commons.utility.depotwarehouse.model;

public class TruckWarehouseAssignment {
    public Depot depot;
    public Warehouse warehouse;
    public Truck truck;
    public double distanceKm;
    public double timeMin;

    public TruckWarehouseAssignment(Depot depot, Warehouse warehouse, Truck truck, double distanceKm, double timeMin) {
        this.depot = depot;
        this.warehouse = warehouse;
        this.truck = truck;
        this.distanceKm = distanceKm;
        this.timeMin = timeMin;
    }

    @Override
    public String toString() {
        return "Depot: " + depot.name + ", Warehouse: " + warehouse.name + ", Truck: " + truck.id
                + ", Distance: " + distanceKm + " km, Time: " + timeMin + " min";
    }
}
