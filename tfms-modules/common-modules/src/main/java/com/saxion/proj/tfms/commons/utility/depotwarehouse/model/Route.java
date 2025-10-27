package com.saxion.proj.tfms.commons.utility.depotwarehouse.model;

public class Route {
    public Depot depot;
    public Warehouse warehouse;

    public Route(Depot depot, Warehouse warehouse) {
        this.depot = depot;
        this.warehouse = warehouse;
    }

    @Override
    public String toString() {
        return depot.name + " -> " + warehouse.name;
    }
}